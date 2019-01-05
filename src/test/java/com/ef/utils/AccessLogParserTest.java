/**
 * @author kudoji
 */
package com.ef.utils;

import com.ef.models.AccessLog;
import com.ef.models.BannedIp;
import com.ef.models.Ip;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccessLogParserTest {
    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;

    private static AccessLogParser accessLogParser;

    @BeforeClass
    public static void initialization(){
        final String accessLogFileName = "accesslog-test.log";

        entityManagerFactory = Persistence.createEntityManagerFactory("mpar-test");
        entityManager = entityManagerFactory.createEntityManager();

        accessLogParser = new AccessLogParser(accessLogFileName, entityManager);
    }

    @AfterClass
    public static void finalization(){
        entityManager.close();
        entityManagerFactory.close();
    }

    @Test
    public void testParse(){
        assertTrue(accessLogParser.parse());

        List<Ip> ipList = entityManager.createQuery("from Ip", Ip.class).getResultList();
        assertEquals(2, ipList.size());

        List<AccessLog> accessLogs = entityManager.createQuery("from AccessLog", AccessLog.class).getResultList();
        assertEquals(5, accessLogs.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBanIpsWithNullStartDate(){
        accessLogParser.banIps(null, CommandLine.DurationValues.daily, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBanIpsWithInvalidDuration(){
        accessLogParser.banIps(LocalDateTime.now(), null, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBanIpsWithInvalidThreshold(){
        accessLogParser.banIps(LocalDateTime.now(), CommandLine.DurationValues.daily, 0);
    }

    @Test
    public void testBanIps(){
        LocalDateTime startDate = LocalDateTime.of(
                2017,
                01,
                01,
                0,
                0,
                11
        );

        assertTrue(accessLogParser.parse());

        Set<String> ipSet = accessLogParser.banIps(startDate, CommandLine.DurationValues.hourly, 2);
        assertEquals(2, ipSet.size());
    }
}
