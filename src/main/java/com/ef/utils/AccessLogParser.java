package com.ef.utils;

import com.ef.models.AccessLog;
import com.ef.models.BannedIp;
import com.ef.models.Ip;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

public class AccessLogParser {
    private final String fileName;
    private final String separator = "\\|";

    //  keep cache of all Ip
    private final Map<String, Ip> cacheIp = new HashMap<>();

    private EntityManager entityManager;

    private final Logger log = Logger.getLogger(AccessLog.class.getName());

    public AccessLogParser(String fileName, EntityManager entityManager){
        if (fileName == null || fileName.isEmpty())
            throw new IllegalArgumentException("File name cannot be empty");

        this.fileName = fileName;

        this.log.setLevel(Level.INFO);

        this.entityManager = entityManager;
    }

    /**
     * Parses file
     *
     * @return true if file successfully parsed, false otherwise
     */
    public boolean parse() {
        entityManager.getTransaction().begin();

        cacheIp.clear();

        boolean isParsed = true;
        try {
            File f = new File(this.fileName);
            FileInputStream is = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            //  truncate tables before adding data to it because parse() can be called multiple times
            entityManager.createQuery("delete AccessLog").executeUpdate();
            entityManager.createQuery("delete Ip").executeUpdate();
            entityManager.createQuery("delete BannedIp").executeUpdate();

            log.info("Lines processed: " + br.lines().map(parseLine).count());

            br.close();
            is.close();
        }catch(FileNotFoundException e){
            isParsed = false;
            log.severe("accesslog file not found (" + this.fileName + ")");
        }catch (IOException e){
            isParsed = false;
            log.severe("cannot open accesslog file (" + this.fileName + ")");
        }

        if (isParsed){
            entityManager.getTransaction().commit();
        }else{
            entityManager.getTransaction().rollback();
        }

        return isParsed;
    }

    /**
     * Taken from here
     * @url https://dzone.com/articles/how-to-read-a-big-csv-file-with-java-8-and-stream
     *
     * with modifications
     *
     */
    private Function<String, AccessLog> parseLine = (line) -> {
        String[] params = line.split(this.separator);

        if (params.length != 5){
            log.severe(format("line '%s' in file is invalid", line));
            return null;
        }

        Ip ip;
        ip = cacheIp.computeIfAbsent(params[1], k -> new Ip(params[1]));

        AccessLog al = new AccessLog(ip);

        try{
            al.setDate(params[0], "yyyy-MM-dd HH:mm:ss.SSS");
        }catch (DateTimeParseException e){
            log.severe(format("line '%s' in file has invalid date '%s'", line, params[0]));

            return null;
        }catch (IllegalArgumentException e){
            log.severe(format("line '%s' in file has empty date '%s'", line, params[0]));

            return null;
        }

        al.setRequest(params[2]);
        al.setStatus(params[3]);
        al.setUserAgent(params[4]);

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        Set<ConstraintViolation<AccessLog>> constraintViolations = validator.validate(al);
        if (constraintViolations.size() > 0){
            log.severe(format("line '%s' has invalid data", line));
            return null;
        }

        entityManager.persist(ip);

        return al;
    };

    /**
     * Finds and bans suspicious IPs based on parameters and adds them to MySQL's table
     *
     *
     * @return List of suspicious IPs
     */
    @SuppressWarnings("unchecked")
    public Set<String> banIps(LocalDateTime startDate, CommandLineWrapper.DurationValues duration, int threshold){
        if (startDate == null) throw new IllegalArgumentException("Start date parameter is invalid");

        ChronoUnit chronoUnit;
        if (duration == CommandLineWrapper.DurationValues.hourly){
            chronoUnit = ChronoUnit.HOURS;
        }else if (duration == CommandLineWrapper.DurationValues.daily){
            chronoUnit = ChronoUnit.DAYS;
        }else{
            //  incorrect duration
            throw new IllegalArgumentException("Duration unit is invalid");
        }

        if (threshold < 1) throw new IllegalArgumentException("Threshold is invalid");

        LocalDateTime endDate = startDate.plus(1, chronoUnit);

        entityManager.getTransaction().begin();

        //  get the list of banned ip
        List<Object[]> ipList = entityManager.createQuery(
                "select al.ip, count(*) as c from AccessLog as al " +
                        "where (al.date >= :date1 and al.date < :date2) " +
                        "group by al.ip having count(*) >= :threshold"
        )
                .setParameter("date1", startDate)
                .setParameter("date2", endDate)
                .setParameter("threshold", (long)threshold)
                .getResultList();

        //  truncate table before adding to it
        entityManager.createQuery("delete BannedIp").executeUpdate();

        Set<String> result = new HashSet<>();
        //  add ips to banned list
        ipList.forEach(objects -> {
            String reason = "banned due to " + duration.name() + " requests more than " + threshold + " (actual: " + objects[1] + ")";
            Ip ip = (Ip)objects[0];
            BannedIp bannedIp = new BannedIp(ip, reason);

            entityManager.persist(ip);

            result.add(ip.getIp());
        });

//        System.out.println(ipsFrequency);
//        System.out.println(ipsFrequency.keySet());

        entityManager.getTransaction().commit();

        return result;
    }
}
