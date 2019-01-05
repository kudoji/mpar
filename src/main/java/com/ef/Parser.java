package com.ef;

import com.ef.models.Ip;
import com.ef.utils.AccessLogParser;
import com.ef.utils.CommandLine;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Set;

public class Parser {
    public static void main(String[] args){
        CommandLine cli = new CommandLine(args);
        if (!cli.parseCommands()){
            System.exit(1);
        }

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("mpar-test");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        AccessLogParser alp = new AccessLogParser(cli.getAccessLog(), entityManager);
        if (alp.parse()){
            Set<String> bannedIps = alp.banIps(cli.getStartDate(), cli.getDuration(), cli.getThreshold());
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
        entityManager.close();
        entityManagerFactory.close();
    }
}
