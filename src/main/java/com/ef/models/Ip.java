/**
 * @author kudoji
 */
package com.ef.models;

import javax.persistence.*;

@Entity
@Table(name = "ips")
public class Ip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(nullable = false)
    private final String ip;

//    public Ip(){
//        this("");
//    }

    public Ip(String ip){
        this.ip = ip;
    }

    public String getIp(){
        return this.ip;
    }
}
