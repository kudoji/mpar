/**
 * @author kudoji
 */
package com.ef.models;

import org.junit.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IpTest {
    private static EntityManagerFactory emf;
    private static Validator validator;

    private EntityManager em;

    @BeforeClass
    public static void initialization(){
        emf = Persistence.createEntityManagerFactory("mpar-test");

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterClass
    public static void finalization(){
        emf.close();
    }

    @Before
    public void beforeTest(){
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    @After
    public void afterTest(){
        em.getTransaction().commit();
        em.close();
    }

    @Test
    public void testIpPersistence(){
        final String ipAddress = "127.0.1.1";
        Ip ip = new Ip(ipAddress);

        em.persist(ip);

        List<Ip> ips = em.createQuery("select ip from Ip as ip", Ip.class).getResultList();
        assertFalse(ips.isEmpty());

        ip = em.find(Ip.class, ip.getId());
        assertEquals(ipAddress, ip.getIp());
        assertEquals(0, ip.getBannedIps().size());
        assertEquals(0, ip.getAccessLogs().size());

        ip = em.find(Ip.class, 1);
        assertEquals(ipAddress, ip.getIp());
    }

    @Test
    public void testBannedIpsPersistence(){
        final String ipAddress = "127.0.0.1";
        final String reason = "banned due to test";

        Ip ip = new Ip(ipAddress);

        BannedIp bannedIp = new BannedIp(ip, reason);
//        ip.getBannedIps().add(bannedIp);

        em.persist(ip);
//        em.persist(bannedIp);

        Ip ip1 = em.find(Ip.class, ip.getId());

        assertEquals(1, ip1.getBannedIps().size());
        assertEquals(reason, ip1.getBannedIps().iterator().next().getReason());

        List<BannedIp> bannedIps = em.createQuery("from BannedIp", BannedIp.class).getResultList();
        assertFalse(bannedIps.isEmpty());
    }

    @Test
    public void testAccessLogPersistence(){
        final String ipAddress = "127.0.0.2";
        final String request = "request";
        final String status = "200";
        final String userAgent = "Safari";

        Ip ip = new Ip(ipAddress);

        AccessLog accessLog = new AccessLog(ip);
        accessLog.setDate(LocalDateTime.now());
        accessLog.setRequest(request);
        accessLog.setStatus(status);
        accessLog.setUserAgent(userAgent);

        em.persist(ip);

        List<AccessLog> accessLogs = em.createQuery(
                "from AccessLog",
                AccessLog.class
        ).getResultList();

        assertFalse(accessLogs.isEmpty());

        accessLog = accessLogs.get(0);
        assertEquals(request, accessLog.getRequest());
        assertEquals(status, accessLog.getStatus());
        assertEquals(userAgent, accessLog.getUserAgent());
    }

    @Test
    public void testIpAddressValidation(){
        final String ipAddressInvalid = "localhost";
        final String ipAddressValid = "10.3.0.234";

        Ip ip = new Ip(ipAddressInvalid);

        Set<ConstraintViolation<Ip>> constraintViolations = validator.validateProperty(ip, "ip");
        assertEquals(1, constraintViolations.size());
        assertEquals("Invalid ip address", constraintViolations.iterator().next().getMessage());


        ip = new Ip(ipAddressValid);

        constraintViolations = validator.validateProperty(ip, "ip");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void testEqualsAndHashCode(){
        Ip ip0 = new Ip("127.0.0.1");
        Ip ip1 = new Ip("127.0.0.1");
        Ip ip2 = new Ip("127.0.0.1");
        Ip ip3 = new Ip("127.0.0.2");

        assertFalse(ip0.equals(null));
        assertFalse(ip0.equals(ip3));

        assertTrue(ip0.equals(ip0));
        assertTrue(ip0.equals(ip1));
        assertTrue(ip1.equals(ip0));
        assertTrue(ip1.equals(ip2));
        assertTrue(ip0.equals(ip2));


        Map<Ip, Integer> ipFreq = new HashMap<>();
        ipFreq.putIfAbsent(ip0, 1);

        assertTrue(ipFreq.containsKey(ip1));

        ipFreq.putIfAbsent(ip1, 2);
        assertEquals(1, ipFreq.size());

        ipFreq.computeIfPresent(ip1, (ip, integer) -> ++integer);

        assertEquals(1, ipFreq.size());
        assertEquals(2, ipFreq.get(ip0).longValue());
    }
}
