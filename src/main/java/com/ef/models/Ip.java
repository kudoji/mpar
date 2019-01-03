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

    @OneToMany(mappedBy = "ip", orphanRemoval = true, cascade = CascadeType.PERSIST)
    private final Set<AccessLog> accessLogs;

    public Ip(){
        this("127.0.0.1");
    }

    public Ip(String ip){
        if (ip.isEmpty()) throw new IllegalArgumentException("ip address cannot be empty");

        this.ip = ip;
        this.bannedIps = new HashSet<>();
        this.accessLogs = new HashSet<>();
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

    public Set<AccessLog> getAccessLogs(){
        return this.accessLogs;
    }
}
