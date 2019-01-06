package com.ef;

import com.ef.utils.AccessLogParser;
import com.ef.utils.CommandLineWrapper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Set;

public class Parser {
    public static void main(String[] args){
        CommandLineWrapper cli = new CommandLineWrapper(args);
        try {
            cli.parseCommands();
        }catch (Exception e){
            System.out.print(e.getMessage());

            System.exit(1);
        }

        System.out.println("parsing access log file, please wait...");

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(
                "mpar-production"
        );
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

        entityManager.close();
        entityManagerFactory.close();

        System.out.println("done!");
    }
}
