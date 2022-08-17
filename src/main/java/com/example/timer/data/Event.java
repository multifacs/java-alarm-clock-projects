package com.example.timer.data;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalTime;

public class Event implements Serializable {
    private int min;
    private int sec;
    private String name;

    public Event(String name, int min, int sec) {
        this.name = name;
        this.min = min;
        this.sec = sec;
    }

    public int getMin() {
        return min;
    }

    public int getSec() {
        return sec;
    }

    public String getName() {
        return name;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setName(String name) {
        this.name = name;
    }
}
