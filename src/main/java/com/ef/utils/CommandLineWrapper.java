package com.ef.utils;

import org.apache.commons.cli.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.String.format;

public class CommandLineWrapper {
    private final Logger log = Logger.getLogger(CommandLineParser.class.getName());

    private final char valueSeparator = '=';

    private static final String CLI_ACCESSLOG_SHORT = "l";
    private static final String CLI_ACCESSLOG_LONG = "accesslog";

    private static final String CLI_STARTDATE_SHORT = "s";
    private static final String CLI_STARTDATE_LONG = "startDate";
    public static final String CLI_STARTDATE_FORMAT = "yyyy-MM-dd.HH:mm:ss";

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

    public CommandLineWrapper(String[] args){
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
        threshold.setRequired(true);
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
     * @throws ParseException, IllegalArgumentException
     */
    public void parseCommands() throws ParseException {
        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine commandLine;

        try{
            commandLine = commandLineParser.parse(this.options, this.args);
        }catch (ParseException e){
            System.err.println(e.getMessage());
            helpFormatter.printHelp("mpar", this.options);

            throw new ParseException(e.getMessage());
        }

        this.accessLog = parseAccessLog(commandLine.getOptionValue(CLI_ACCESSLOG_LONG));
        this.startDate = parseStartDate(commandLine.getOptionValue(CLI_STARTDATE_LONG));
        this.duration = parseDuration(commandLine.getOptionValue(CLI_DURATION_LONG));
        this.threshold = parseThreshold(commandLine.getOptionValue(CLI_THRESHOLD_LONG));
    }

    /**
     * @throws IllegalArgumentException
     * @param accessLog
     * @return
     */
    private String parseAccessLog(String accessLog) {
        File al = new File(accessLog);

        if (!al.exists()){
            String message = "the access log file does not exist";
            log.severe(message);

            throw new IllegalArgumentException(message);
        }

        return accessLog;
    }

    /**
     * @throws IllegalArgumentException
     * @param startDate
     * @return
     */
    private LocalDateTime parseStartDate(String startDate) {
        LocalDateTime date;

        try{
            date = LocalDateTime.parse(
                    startDate,
                    DateTimeFormatter.ofPattern(
                            CLI_STARTDATE_FORMAT,
                            Locale.ENGLISH
                    ));
        }catch (DateTimeParseException e){
            String message = format("incorrect date, supports the following format only: '%s'", CLI_STARTDATE_FORMAT);
            log.severe(message);
            throw new IllegalArgumentException(message);
        }

        return date;
    }

    /**
     * @throws IllegalArgumentException
     * @param duration
     * @return
     */
    private DurationValues parseDuration(String duration) {
        if (!duration.equals(DurationValues.daily.name()) &&
                !duration.equals(DurationValues.hourly.name())){

            String message = "duration parameter is incorrect; two parameters acceptable: 'hourly' and 'daily'";
            log.severe(message);
            throw new IllegalArgumentException(message);
        }

        return DurationValues.valueOf(duration);
    }

    /**
     * @throws IllegalArgumentException
     * @param threshold
     * @return
     */
    private int parseThreshold(String threshold) {
        boolean result = false;
        String message = "";
        try{
            result = Integer.parseInt(threshold) > 0;
        }catch (NumberFormatException e){
            message = "threshold parameter is invalid";
            log.severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!result){
            message = "threshold parameter should be more than zero";
            log.severe(message);
            throw new IllegalArgumentException(message);
        }

        return Integer.parseInt(threshold);
    }
}
