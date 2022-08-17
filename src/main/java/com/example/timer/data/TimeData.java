package com.example.timer.data;

import java.io.Serializable;
import java.time.LocalTime;

public class TimeData implements Serializable {
    private LocalTime time;

    public TimeData() {
         time = LocalTime.of(0, 0, 0);
    }

    public TimeData(LocalTime time) {
        this.time = time;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
