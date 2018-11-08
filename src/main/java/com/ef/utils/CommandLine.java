package com.ef.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommandLine {
    private String[] args;
    private String accessLog;
    private DurationValues duration;
    private Date startDate;
    private int threshold;
    private HashMap<String, String> params;
    private static final String CLI_ACCESSLOG = "accesslog";
    private static final String CLI_STARTDATE = "startDate";
    private static final String CLI_DURATION = "duration";
    private static final String CLI_THRESHOLD = "threshold";
    public enum DurationValues{
        hourly,
        daily
    }

    public CommandLine(String[] args){
        this.args = args;

        this.params = new HashMap<>();
        this.params.put(CLI_ACCESSLOG, "");
        this.params.put(CLI_STARTDATE, "");
        this.params.put(CLI_DURATION, "");
        this.params.put(CLI_THRESHOLD, "");
    }

    public String getAccessLog(){
        return this.accessLog;
    }

    public Date getStartDate(){
        return this.startDate;
    }

    public DurationValues getDuration(){
        return this.duration;
    }

    public int getThreshold(){
        return this.threshold;
    }

    /**
     * Extracts all necessary commandline params
     *
     * @return
     */
    public boolean parseCommands(){
        if (this.args.length != 4){
            System.out.println("4 parameters required.");
            System.out.println();
            System.out.println("usage: ");
            System.out.println("\t--" + CLI_ACCESSLOG + "=/path/to/file");
            System.out.println("\t--" + CLI_STARTDATE + "=2017-01-01.13:00:00");
            System.out.println("\t--" + CLI_DURATION + "=hourly");
            System.out.println("\t--" + CLI_THRESHOLD + "=100");

            return false;
        }

        for (String arg: args){
            Iterator<Map.Entry<String, String>> iterator = this.params.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> pair = (Map.Entry<String, String>)iterator.next();

                String searchParam = "--" + pair.getKey() + "=";
                if (arg.startsWith(searchParam)){
                    pair.setValue(arg.substring(searchParam.length()));

                    break;
                }
            }
        }

        if (!parseAccessLog()){
            return false;
        }

        if (!parseStartDate()){
            return false;
        }

        if (!parseDuration()){
            return false;
        }

        if (!parseThreshold()){
            return false;
        }

        return true;
    }

    private boolean parseAccessLog(){
        String acclessLog = this.params.get(CLI_ACCESSLOG);

        File al = new File(acclessLog);

        if (!al.exists()){
            System.err.println("\taccesslog file does not exist");
            return false;
        }

        this.accessLog = acclessLog;

        return true;
    }

    private boolean parseStartDate(){
        boolean isDateCorrect = true;
        String startDate = this.params.get(CLI_STARTDATE);
        if (startDate.isEmpty()){
            System.err.println("\tstartDate parameter is not set");
            return false;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss", Locale.ENGLISH);
        Date date = new Date();
        try{
            date = df.parse(startDate);
        }catch (Exception e){
            isDateCorrect = false;
//            e.printStackTrace();
        }

        if (!isDateCorrect){
            System.err.println("\tincorrect date, supports the following format only: 'yyyy-MM-dd.HH:mm:ss'");
            return false;
        }

        this.startDate = date;

        return isDateCorrect;
    }

    private boolean parseDuration(){
        String duration = this.params.get(CLI_DURATION);
        if (duration.isEmpty()){
            System.err.println("\tduration parameter is not set");
            return false;
        }

        if (!duration.equals(DurationValues.daily.name()) &&
                !duration.equals(DurationValues.hourly.name())){
            System.err.println("\tduration parameter is incorrect; two parameters acceptable: 'hourly', 'daily'");

            return false;
        }

        if (duration.equals(DurationValues.daily.name())){
            this.duration = DurationValues.daily;
        }else{
            this.duration = DurationValues.hourly;
        }

        return true;
    }

    private boolean parseThreshold(){
        String threshold = this.params.get(CLI_THRESHOLD);
        if (threshold.isEmpty()){
            System.err.println("\tthreshold parameter is not set");
            return false;
        }

        boolean result = Integer.parseInt(threshold) > 0;
        if (!result){
            System.err.println("\tthreshold parameter should be more than zero");

            return false;
        }

        this.threshold = Integer.parseInt(threshold);

        return result;
    }
}
