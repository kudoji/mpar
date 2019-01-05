/**
 * @author kudoji
 */
package com.ef.models;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

import static org.junit.Assert.assertEquals;


public class AccessLogTest {
    //  majority covered in IpTest.java

    private static Validator validator;

    private void assertError(AccessLog accessLog, String property, String errorMessage){
        Set<ConstraintViolation<AccessLog>> constraintViolations =
                validator.validateProperty(accessLog, property);

        assertEquals(1, constraintViolations.size());
        assertEquals(errorMessage, constraintViolations.iterator().next().getMessage());
    }

    private void assertNoError(AccessLog accessLog, String property){
        Set<ConstraintViolation<AccessLog>> constraintViolations =
                validator.validateProperty(accessLog, property);

        assertEquals(0, constraintViolations.size());
    }

    @BeforeClass
    public static void initialization(){
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithIllegalIp(){
        AccessLog accessLog = new AccessLog(null);
    }

    @Test
    public void testDateValidation(){
        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setDate(null);
        assertError(accessLog, "date", "Date is invalid");

        accessLog.setDate(LocalDateTime.now());
        assertNoError(accessLog, "date");
    }

    @Test
    public void testRequestValidation(){
        final String requestInvalid1 = "sm";
        final String requestInvalid2 = "too long request, too long request, too long request, too long request, too long request, too long request, too long request, ";
        final String requestValid = "\"GET / HTTP/1.1\"";

        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setRequest(null);
        assertError(accessLog, "request", "Request is invalid");

        accessLog.setRequest(requestInvalid1);
        assertError(accessLog, "request", "Request is invalid");

        accessLog.setRequest(requestInvalid2);
        assertError(accessLog, "request", "Request is invalid");

        accessLog.setRequest(requestValid);
        assertNoError(accessLog, "request");
    }

    @Test
    public void testStatusValidation(){
        final String statusInvalid1 = null;
        final String statusInvalid2 = "";
        final String statusInvalid3 = "sd";
        final String statusInvalid4 = "too long";
        final String statusValid = "200";

        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setStatus(statusInvalid1);
        assertError(accessLog, "status", "Status is invalid");

        accessLog.setStatus(statusInvalid2);
        assertError(accessLog, "status", "Status is invalid");

        accessLog.setStatus(statusInvalid3);
        assertError(accessLog, "status", "Status is invalid");

        accessLog.setStatus(statusInvalid4);
        assertError(accessLog, "status", "Status is invalid");

        accessLog.setStatus(statusValid);
        assertNoError(accessLog, "status" );
    }

    @Test
    public void testUserAgentValidation(){
        final String userAgentInvalid = "too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, ";
        final String userAgentValid1 = null;
        final String userAgentValid2 = "";
        final String userAgentValid3 = "\"Mozilla/5.0 (Linux; Android 7.0; Nexus 6P Build/NBD91K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.107 Mobile Safari/537.36\"";

        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setUserAgent(userAgentInvalid);
        assertError(accessLog, "userAgent", "User agent is invalid");

        accessLog.setUserAgent(userAgentValid1);
        assertNoError(accessLog, "userAgent");

        accessLog.setUserAgent(userAgentValid2);
        assertNoError(accessLog, "userAgent");

        accessLog.setUserAgent(userAgentValid3);
        assertNoError(accessLog, "userAgent");
    }

    @Test
    public void testSetDateMethodValid(){
        final String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        final String dateString = "2017-12-21 00:00:11.763";
        final LocalDateTime date = LocalDateTime.of(
                2017,
                12,
                21,
                0,
                0,
                11,
                763 * 1000000 // convert to milliseconds which SSS is
        );

        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setDate(dateString, pattern);
        assertEquals(date, accessLog.getDate());
    }

    @Test(expected = DateTimeParseException.class)
    public void testSetDateMethodInvalidDate(){
        final String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        final String dateString = "2017-22-21 00:00:11.763";

        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setDate(dateString, pattern);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateMethodNullDate(){
        final String pattern = "yyyy-MM-dd HH:mm:ss.SSS";

        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setDate(null, pattern);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateMethodEmptyDate(){
        final String pattern = "yyyy-MM-dd HH:mm:ss.SSS";

        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setDate("", pattern);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateMethodNullPattern(){
        final String dateString = "2017-12-21 00:00:11.763";

        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setDate(dateString, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateMethodEmptyPattern(){
        final String dateString = "2017-12-21 00:00:11.763";

        Ip ip = new Ip();
        AccessLog accessLog = new AccessLog(ip);

        accessLog.setDate(dateString, "");
    }
}
