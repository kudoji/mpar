/**
 * @author kudoji
 */
package com.ef.utils;

import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.ef.utils.CommandLineWrapper.CLI_STARTDATE_FORMAT;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

public class CommandLineWrapperTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test(expected = ParseException.class)
    public void testParseCommandsWithNoArgs() throws ParseException{
        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(new String[0]);
        commandLineWrapper.parseCommands();
    }

    @Test(expected = ParseException.class)
    public void testParseCommandsWithNoAccessLogArg() throws ParseException{
        String[] args = new String[3];
        args[0] = "--duration=hourly";
        args[1] = "--startDate=2017-01-01.13:00:00";
        args[2] = "--threshold=100";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test(expected = ParseException.class)
    public void testParseCommandsWithNoStartDateArg() throws ParseException{
        String[] args = new String[3];
        args[0] = "--duration=hourly";
        args[1] = "--accesslog=2017-01-01.13:00:00";
        args[2] = "--threshold=100";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test(expected = ParseException.class)
    public void testParseCommandsWithNoDurationArg() throws ParseException{
        String[] args = new String[3];
        args[0] = "--accesslog=hourly";
        args[1] = "--startDate=2017-01-01.13:00:00";
        args[2] = "--threshold=100";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test(expected = ParseException.class)
    public void testParseCommandsWithNoThresholdArg() throws ParseException{
        String[] args = new String[3];
        args[0] = "--accesslog=hourly";
        args[1] = "--startDate=2017-01-01.13:00:00";
        args[2] = "--duration=";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommandsWithInvalidAccessLogArg() throws ParseException{
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("the access log file does not exist");

        String[] args = new String[4];
        args[0] = "--accesslog=/varmpar";
        args[1] = "--duration=hourly";
        args[2] = "--startDate=2017-01-01.13:00:00";
        args[3] = "--threshold=100";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommandsWithEmptyAccessLogArg() throws ParseException{
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("the access log file does not exist");

        String[] args = new String[4];
        args[0] = "--accesslog=";
        args[1] = "--duration=hourly";
        args[2] = "--startDate=2017-01-01.13:00:00";
        args[3] = "--threshold=100";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommandsWithEmptyStartDateArg() throws ParseException{
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(format("incorrect date, supports the following format only: '%s'", CLI_STARTDATE_FORMAT));

        String[] args = new String[4];
        args[0] = "--accesslog=access.log";
        args[1] = "--duration=hourly";
        args[2] = "--startDate=";
        args[3] = "--threshold=100";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommandsWithInvalidStartDateArg() throws ParseException{
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(format("incorrect date, supports the following format only: '%s'", CLI_STARTDATE_FORMAT));

        String[] args = new String[4];
        args[0] = "--accesslog=access.log";
        args[1] = "--duration=hourly";
        args[2] = "--startDate=2017-21-01.13:00:00";
        args[3] = "--threshold=100";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommandsWithEmptyDurationArg() throws ParseException{
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("duration parameter is incorrect; two parameters acceptable: 'hourly' and 'daily'");

        String[] args = new String[4];
        args[0] = "--accesslog=access.log";
        args[1] = "--duration=";
        args[2] = "--startDate=2017-01-01.13:00:00";
        args[3] = "--threshold=100";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommandsWithInvalidDurationArg() throws ParseException{
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("duration parameter is incorrect; two parameters acceptable: 'hourly' and 'daily'");

        String[] args = new String[4];
        args[0] = "--accesslog=access.log";
        args[1] = "--duration=annually";
        args[2] = "--startDate=2017-01-01.13:00:00";
        args[3] = "--threshold=100";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommandsWithEmptyThresholdArg() throws ParseException{
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("threshold parameter is invalid");

        String[] args = new String[4];
        args[0] = "--accesslog=access.log";
        args[1] = "--duration=hourly";
        args[2] = "--startDate=2017-01-01.13:00:00";
        args[3] = "--threshold=";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommandsWithInvalidThresholdArg() throws ParseException{
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("threshold parameter is invalid");

        String[] args = new String[4];
        args[0] = "--accesslog=access.log";
        args[1] = "--duration=hourly";
        args[2] = "--startDate=2017-01-01.13:00:00";
        args[3] = "--threshold=jie10";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommandsWithNegativeThresholdArg() throws ParseException{
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("threshold parameter should be more than zero");

        String[] args = new String[4];
        args[0] = "--accesslog=access.log";
        args[1] = "--duration=hourly";
        args[2] = "--startDate=2017-01-01.13:00:00";
        args[3] = "--threshold=-10";

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();
    }

    @Test
    public void testParseCommands() throws ParseException{
        String accesslog = "access.log";
        CommandLineWrapper.DurationValues durationValues = CommandLineWrapper.DurationValues.hourly;
        LocalDateTime localDateTime = LocalDateTime.of(
                2017,
                01,
                01,
                13,
                00,
                00
        );
        int threshold = 10;

        String[] args = new String[4];
        args[0] = "--accesslog=" + accesslog;
        args[1] = "--duration=" + durationValues.name();
        args[2] = "--startDate=" + localDateTime.format(DateTimeFormatter.ofPattern(CLI_STARTDATE_FORMAT, Locale.ENGLISH));
        args[3] = "--threshold=" + threshold;

        CommandLineWrapper commandLineWrapper = new CommandLineWrapper(args);
        commandLineWrapper.parseCommands();

        assertEquals(accesslog, commandLineWrapper.getAccessLog());
        assertEquals(durationValues, commandLineWrapper.getDuration());
        assertEquals(localDateTime, commandLineWrapper.getStartDate());
        assertEquals(threshold, commandLineWrapper.getThreshold());
    }
}
