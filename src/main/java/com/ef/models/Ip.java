/**
 * @author kudoji
 */
package com.ef.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

//@Data //lombok causes StackOverflow...
@Entity
@Table(name = "ips")
public class Ip {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Pattern(regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$", message = "Invalid ip address")
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
        if (ip == null || ip.isEmpty()) throw new IllegalArgumentException("ip address cannot be empty");

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

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof Ip)) return false;

        Ip ip = (Ip)obj;

        //  BOTH ip and ip MUST be the same at the same time
        return (this.id == ip.getId()) & (this.ip == ip.getIp());
    }

    @Override
    public int hashCode(){
        return this.id + 7 * this.ip.hashCode();
    }
}
