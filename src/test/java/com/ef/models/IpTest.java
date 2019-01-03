/**
 * @author kudoji
 */
package com.ef.models;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class IpTest {
    private static EntityManagerFactory emf;

    @BeforeClass
    public static void initialization(){
        emf = Persistence.createEntityManagerFactory("mpar-test");
    }

    @AfterClass
    public static void finalization(){
        emf.close();
    }

    @Test
    public void writeDataToDB(){
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        final String ipAddress = "127.0.1.1";
        Ip ip = new Ip(ipAddress);

        em.persist(ip);
        em.getTransaction().commit();

        em.getTransaction().begin();

        List<Ip> ips = em.createQuery("select ip from Ip as ip", Ip.class).getResultList();

        assertEquals(1, ips.size());
        assertEquals(ipAddress, ips.get(0).getIp());

        ip = em.find(Ip.class, 1);
        assertEquals(ipAddress, ip.getIp());

        em.getTransaction().commit();

        em.close();
    }
}
