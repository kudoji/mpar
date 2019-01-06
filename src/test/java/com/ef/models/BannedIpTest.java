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
import java.util.Set;

import static org.junit.Assert.assertEquals;


public class BannedIpTest {
    //  majority covered in IpTest.java

    private static Validator validator;

    @BeforeClass
    public static void initialization(){
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithIllegalIp(){
        BannedIp bannedIp = new BannedIp(null, "reason");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullReason(){
        Ip ip = new Ip();
        BannedIp bannedIp = new BannedIp(ip, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithIllegalReason(){
        Ip ip = new Ip();
        BannedIp bannedIp = new BannedIp(ip, "");
    }

    @Test
    public void testReasonValidation(){
        final String reasonInvalid1 = "smll";
        final String reasonInvalid2 = "too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, too long and tedious, ";
        final String reasonValid = "banned due to hourly requests more than 100 (actual: 228)";

        Ip ip = new Ip();
        BannedIp bannedIp = new BannedIp(ip, reasonInvalid1);
        Set<ConstraintViolation<BannedIp>> constraintViolations = validator.validateProperty(
                bannedIp, "reason");
        assertEquals(1, constraintViolations.size());
        assertEquals("Reason is invalid", constraintViolations.iterator().next().getMessage());

        bannedIp = new BannedIp(ip, reasonInvalid2);
        constraintViolations = validator.validateProperty(bannedIp, "reason");
        assertEquals(1, constraintViolations.size());
        assertEquals("Reason is invalid", constraintViolations.iterator().next().getMessage());

        bannedIp = new BannedIp(ip, reasonValid);
        constraintViolations = validator.validateProperty(bannedIp, "reason");
        assertEquals(0, constraintViolations.size());
    }
}
