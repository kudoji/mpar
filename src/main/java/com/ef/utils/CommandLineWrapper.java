package com.ef.utils;

import org.apache.commons.cli.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CommandLine {
    private final char valueSeparator = '=';

    private static final String CLI_ACCESSLOG_SHORT = "l";
    private static final String CLI_ACCESSLOG_LONG = "accesslog";

    private static final String CLI_STARTDATE_SHORT = "s";
    private static final String CLI_STARTDATE_LONG = "startDate";

    private static final String CLI_DURATION_SHORT = "d";
    private static final String CLI_DURATION_LONG = "duration";

    private static final String CLI_THRESHOLD_SHORT = "t";
    private static final String CLI_THRESHOLD_LONG = "threshold";

    private final String[] args;
    private final Options options;


    private String accessLog;
    private DurationValues duration;
    private LocalDateTime startDate;
    private int threshold;
    public enum DurationValues{
        hourly,
        daily
    }

    public CommandLine(String[] args){
        this.options = new Options();

        Option accesslog = new Option(
                CLI_ACCESSLOG_SHORT,
                CLI_ACCESSLOG_LONG,
                true,
                "path to web server access log file"
        );
        accesslog.setValueSeparator(valueSeparator);
        accesslog.setRequired(true);
        options.addOption(accesslog);

        Option startDate = new Option(
                CLI_STARTDATE_SHORT,
                CLI_STARTDATE_LONG,
                true,
                "start date of 'yyyy-MM-dd.HH:mm:ss' format"
        );
        startDate.setValueSeparator(valueSeparator);
        startDate.setRequired(true);
        options.addOption(startDate);

        Option duration = new Option(
                CLI_DURATION_SHORT,
                CLI_DURATION_LONG,
                true,
                "can take only 'hourly', 'daily' as inputs"
        );
        duration.setValueSeparator(valueSeparator);
        duration.setRequired(true);
        options.addOption(duration);

        Option threshold = new Option(
                CLI_THRESHOLD_SHORT,
                CLI_THRESHOLD_LONG,
                true,
                "IP with more than specified amount hits will be banned"
        );
        threshold.setValueSeparator(valueSeparator);
        duration.setRequired(true);
        options.addOption(threshold);

        this.args = args;
    }

    public String getAccessLog(){
        return this.accessLog;
    }

    public LocalDateTime getStartDate(){
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
    public void parseCommands() throws ParseException {
        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine commandLine;

        commandLine = commandLineParser.parse(this.options, this.args);


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

        //  TODO: must throw exception
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

        LocalDateTime date = LocalDateTime.now();

        try{
            date = LocalDateTime.parse(
                    startDate,
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd.HH:mm:ss",
                            Locale.ENGLISH
                    ));
        }catch (DateTimeParseException e){
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
