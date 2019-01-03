/**
 * @author kudoji
 */
package com.ef.models;

import javax.persistence.*;

@Entity
@Table(name = "banned_ips")
public class BannedIp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Ip ip;

    @Column(nullable = false)
    private String reason;

    public BannedIp(Ip ip, String reason){
        if (ip == null) throw new IllegalArgumentException("Ip object cannot be null");
        if (reason.isEmpty()) throw new IllegalArgumentException("Reason cannot be empty");

        this.ip = ip;
        this.reason = reason;
    }

    public String getReason(){
        return this.reason;
    }
}
