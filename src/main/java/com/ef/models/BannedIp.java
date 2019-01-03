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
        this.ip = ip;
        this.reason = reason;
    }

    public String getReason(){
        return this.reason;
    }
}
