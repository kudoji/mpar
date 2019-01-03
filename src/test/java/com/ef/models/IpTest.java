/**
 * @author kudoji
 */
package com.ef.models;

import org.junit.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class IpTest {
    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeClass
    public static void initialization(){
        emf = Persistence.createEntityManagerFactory("mpar-test");
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

        ip = em.find(Ip.class, 1);
        assertEquals(ipAddress, ip.getIp());
    }

    @Test
    public void testBannedIpsPersistence(){
        final String ipAddress = "127.0.0.1";
        final String reason = "banned due to test";

        Ip ip = new Ip(ipAddress);

        BannedIp bannedIp = new BannedIp(ip, reason);
        ip.getBannedIps().add(bannedIp);

        em.persist(ip);
//        em.persist(bannedIp);

        Ip ip1 = em.find(Ip.class, ip.getId());

        assertEquals(1, ip1.getBannedIps().size());
        assertEquals(reason, ip1.getBannedIps().iterator().next().getReason());

        List<BannedIp> bannedIps = em.createQuery("from BannedIp", BannedIp.class).getResultList();
        assertFalse(bannedIps.isEmpty());
    }
}
