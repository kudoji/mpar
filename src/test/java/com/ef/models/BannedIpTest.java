/**
 * @author kudoji
 */
package com.ef.models;

import org.junit.Test;

public class BannedIpTest {
    //  majority covered in IpTest.java

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithIllegalIp(){
        BannedIp bannedIp = new BannedIp(null, "reason");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithIllegalReason(){
        Ip ip = new Ip();
        BannedIp bannedIp = new BannedIp(ip, "");
    }
}
