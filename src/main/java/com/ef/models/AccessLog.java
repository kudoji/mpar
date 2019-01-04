/**
 * @author kudoji
 */
package com.ef.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Data //lombok
@Entity
@Table(name = "accesslog_data")
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull(message = "Date is invalid")
    @Column(nullable = false)
    private LocalDateTime date;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private final Ip ip;

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

        this.ip.getAccessLogs().add(this);
    }

    /**
     *
     * @param date
     * @param pattern
     *
     * @throws DateTimeParseException if the text cannot be parsed
     */
    public void setDate(String date, String pattern) {
        if (date == null || date.isEmpty()) throw new IllegalArgumentException(
                "Date parameter cannot be null or empty");
        if (pattern == null || pattern.isEmpty()) throw new IllegalArgumentException(
                "Pattern parameter cannot be null or empty");

        this.date = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }
}
