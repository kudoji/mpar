/**
 * @author kudoji
 */
package com.ef.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "banned_ips")
public class BannedIp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull(message = "Ip object cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private final Ip ip;

    @Size(min = 5, max = 150, message = "Reason is invalid")
    @Column(nullable = false)
    private final String reason;

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
