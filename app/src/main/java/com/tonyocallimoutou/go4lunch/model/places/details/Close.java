package com.tonyocallimoutou.go4lunch.model.places.details;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Close {

    @SerializedName("day")
    @Expose
    private Integer day;
    @SerializedName("time")
    @Expose
    private String time;

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    // Constructor for Test

    public Close() {
    }

    public Close(Integer day, String time) {
        this.day = day;
        this.time = time;
    }
}
