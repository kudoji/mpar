package com.ef.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AccessLog file model
 */
public class AccessLog {
    private int id;
    private Date date;
    private String ip;
    private String request;
    //  should be int, but let it be String in case
    private String status;
    private String userAgent;

    public AccessLog(){
    }

    public Date getDate(){
        return this.date;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public boolean setDate(String dateString){
        boolean isDateCorrect = true;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
        Date date = new Date();
        try{
            date = df.parse(dateString);
        }catch (Exception e){
            isDateCorrect = false;
            e.printStackTrace();
        }

        this.date = date;

        return isDateCorrect;
    }

    public String getIp(){
        return this.ip;
    }

    public void setIp(String ip){
        this.ip = ip;
    }

    public String getRequest(){
        return this.request;
    }

    public void setRequest(String request){
        this.request = request;
    }

    public String getStatus(){
        return this.status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getUserAgent(){
        return this.userAgent;
    }

    public void setUserAgent(String userAgent){
        this.userAgent = userAgent;
    }
}
