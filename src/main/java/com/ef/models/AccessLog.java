/**
 * @author kudoji
 */
package com.ef.models;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data //lombok
@Entity
@Table(name = "accesslog_data")
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Ip ip;

    @Column(nullable = false)
    private String request;

    @Column(nullable = false)
    private String status;

    @Column(name = "user_agent", nullable = true)
    private String userAgent;

    public AccessLog(Ip ip){
        if (ip == null) throw new IllegalArgumentException("Ip cannot be null");

        this.ip = ip;
    }
}
