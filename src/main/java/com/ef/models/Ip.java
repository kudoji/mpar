/**
 * @author kudoji
 */
package com.ef.models;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ips")
public class Ip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false, unique = true)
    private final String ip;

    @OneToMany(mappedBy = "ip", orphanRemoval = true, cascade = CascadeType.PERSIST)
    private final Set<BannedIp> bannedIps;

    public Ip(){
        this("");
    }

    public Ip(String ip){
        this.ip = ip;
        this.bannedIps = new HashSet<>();
    }

    public int getId(){
        return this.id;
    }

    public String getIp(){
        return this.ip;
    }

    public Set<BannedIp> getBannedIps(){
        return this.bannedIps;
    }
}
