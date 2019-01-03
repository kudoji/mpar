/**
 * @author kudoji
 */
package com.ef.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data //lombok
@Entity
@Table(name = "accesslog_data")
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull(message = "Date is invalid")
    @Column(nullable = false)
    private LocalDate date;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Ip ip;

    @NotNull(message = "Request is invalid")
    @Size(min = 5, max = 75, message = "Request is invalid")
    @Column(nullable = false)
    private String request;

    @NotNull(message = "Status is invalid")
    @Size(min = 3, max = 5, message = "Status is invalid")
    @Column(nullable = false)
    private String status;

    @Size(max = 255, message = "User agent is invalid")
    @Column(name = "user_agent", nullable = true)
    private String userAgent;

    public AccessLog(Ip ip){
        if (ip == null) throw new IllegalArgumentException("Ip cannot be null");

        this.ip = ip;
    }
}
