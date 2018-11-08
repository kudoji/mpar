package com.ef.utils;

import com.ef.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccessLogParser {
    private String fileName;
    private String separator;
    private List<AccessLog> data;
    private DB mysql;
    //  keeps flag that connection to MySQL is established or not
    private boolean isConnected;

    public AccessLogParser(String fileName){
        this.fileName = fileName;
        this.separator = "\\|";

        this.mysql = new DB(Parser.MYSQL_URL, Parser.MYSQL_LOGIN, Parser.MYSQL_PASSWORD, Parser.MYSQL_DATABASE);
        this.mysql.setDebugMode(true);
        this.isConnected = this.mysql.connect();
    }

    public List<AccessLog> getData(){
        return this.data;
    }

    /**
     * Parses file
     *
     * @return true if file successfully parsed, false otherwise
     */
    public boolean parse(){
        boolean isParsed = true;
        try{
            File f = new File(this.fileName);
            FileInputStream is = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            if (this.isConnected){
                //  truncate table before adding to it
                this.mysql.execSQL("truncate table `" + Parser.MYSQL_DATABASE + "`.`" + Parser.MYSQL_ACCESSLOG_TABLE + "`;");
            }

            this.data = br.lines().map(parseLine).collect(Collectors.toList());

            br.close();
            is.close();
        }catch (Exception e){
            //  something went wrong...
            isParsed = false;
            System.err.println("\taccesslog file is incorrect");
//            e.printStackTrace();
        }

        return isParsed;
    }

    /**
     * Taken from here
     * @url https://dzone.com/articles/how-to-read-a-big-csv-file-with-java-8-and-stream
     *
     * TODO make sure that file format is expected (5 columns)
     * TODO check that loaded data is valid (e.g. date is correct, ip is in following format)
     */
    private Function<String, AccessLog> parseLine = (line) -> {
        String[] params = line.split(this.separator);

        AccessLog al = new AccessLog();

        al.setDate(params[0]);
        al.setIp(params[1]);
        al.setRequest(params[2]);
        al.setStatus(params[3]);
        al.setUserAgent(params[4]);

        //  lets add this data MySQL table
        if (this.isConnected){
            //  MySQL is connected
            HashMap<String, String> data = new HashMap<>();
            data.put("table", Parser.MYSQL_ACCESSLOG_TABLE);
            data.put("date", (new java.sql.Timestamp(al.getDate().getTime())).toString());
            data.put("ip", al.getIp());
            data.put("request", al.getRequest());
            data.put("status", al.getStatus());
            data.put("user_agent", al.getUserAgent());

            int id = this.mysql.updateData(true, data);
            if (id > 0){
                //  keep MySQL id to use it later
                al.setId(id);
            }else{
                System.err.println("MySQL error for line: '" + line + "'");
            }
        }


        return al;
    };

    /**
     * Finds and bans suspicious IPs based on parameters and adds them to MySQL's table
     *
     *
     * @return List of suspicious IPs
     */
    public List<String> banIps(Date startDate, CommandLine.DurationValues duration, int threshold){
        List<String> ips = new ArrayList<>();

        Date endDate = startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        if (duration == CommandLine.DurationValues.hourly){
            calendar.add(Calendar.HOUR, 1);
            endDate = calendar.getTime();
        }else if (duration == CommandLine.DurationValues.daily){
            calendar.add(Calendar.DATE, 1);
            endDate = calendar.getTime();
        }else{
            //  incorrect duration
            return ips;
        }

        //  keep ips freq usage in hash map
        HashMap<String, Integer> ipsFrequency = new HashMap<>();
        //  assume that accesslog's data is not sorted by date
        //  we can sort using comparator but it makes no sense since it's going to use the same cycle
        for (AccessLog al: this.data){
            Date alDate = al.getDate();
            if ( (alDate.compareTo(startDate) >= 0) && (alDate.compareTo(endDate) < 0) ){
                //  alDate belongs to [startDate; endDate) interval
                int ipFrequency = 0;
                String ip = al.getIp();
                if (ipsFrequency.get(ip) != null){
                    ipFrequency = ipsFrequency.get(ip);
                    ipFrequency++;
                }
                ipsFrequency.put(ip, ipFrequency);
            }
        }

        //  filter ipsFrequency by threshold
        Iterator<Integer> integerIterator = ipsFrequency.values().iterator();
        while (integerIterator.hasNext()){
            int freq = integerIterator.next();

            if (freq < threshold){
                integerIterator.remove();
            }
        }

        if (this.isConnected){
            //  add ips to banned list
            //  truncate table before adding to it
            this.mysql.execSQL("truncate table `" + Parser.MYSQL_DATABASE + "`.`" + Parser.MYSQL_BANNEDIPS_TABLE + "`;");

            HashMap<String, String> params = new HashMap<>();

            Iterator<Map.Entry<String, Integer>> entryIterator = ipsFrequency.entrySet().iterator();
            while (entryIterator.hasNext()){
                Map.Entry<String, Integer> me = entryIterator.next();

                params.put("table", Parser.MYSQL_BANNEDIPS_TABLE);
                params.put("ip", me.getKey());
                params.put("reason", "banned due to " + duration.name() + " requests more than " + threshold + " (actual: " + me.getValue() + ")");

                this.mysql.updateData(true, params);
            }
        }

//        System.out.println(ipsFrequency);
//        System.out.println(ipsFrequency.keySet());

        return new ArrayList<String>(ipsFrequency.keySet());
    }
}
