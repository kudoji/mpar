package com.ef;

import com.ef.utils.AccessLogParser;
import com.ef.utils.CommandLine;

import java.util.List;

public class Parser {
    public static final String MYSQL_URL = "127.0.0.1";
    public static final String MYSQL_LOGIN = "root";
    public static final String MYSQL_PASSWORD = "";
    public static final String MYSQL_DATABASE = "accesslog";
    public static final String MYSQL_ACCESSLOG_TABLE = "accesslog_data";
    public static final String MYSQL_BANNEDIPS_TABLE = "banned_ips";

    public static void main(String[] args){
        CommandLine cli = new CommandLine(args);
        if (!cli.parseCommands()){
            System.exit(1);
        }

        AccessLogParser alp = new AccessLogParser(cli.getAccessLog());
        if (alp.parse()){
            List<String> bannedIps = alp.banIps(cli.getStartDate(), cli.getDuration(), cli.getThreshold());
            if (bannedIps.isEmpty()){
                System.out.println("there are no suspicious ips found");
            }else{
                System.out.println("banned ips:");
                for (String ip: bannedIps){
                    System.out.println("\tip: " + ip);
                }
            }
        }
//        System.out.println(alp.getData().size());
    }
}
