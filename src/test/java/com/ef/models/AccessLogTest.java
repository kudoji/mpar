/**
 * @author kudoji
 */
package com.ef.models;

import org.junit.Test;

public class AccessLogTest {
    //  majority covered in IpTest.java

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithIllegalIp(){
        AccessLog accessLog = new AccessLog(null);
    }
}
