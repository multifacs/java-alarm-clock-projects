package com.example.timer.data;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Timer {
    
    private int min;
    private int sec;

    public Timer() {
        this.min = 0;
        this.sec = 0;
    }

    public String getTimeString() {
        return String.format("%02d:" , this.min) + String.format("%02d" , this.sec);
    }

    public void incTime() {

        this.sec += 1;
        if (sec == 60) {
            sec = 0;
            min = (min + 1) % 60;
        }
    }

    public int getMin() {
        return min;
    }

    public int getSec() {
        return sec;
    }

    public boolean compare(Event event) {
        int eventSec = event.getMin() * 60 + event.getSec();
        int timerSec = this.min * 60 + this.sec;

        return eventSec <= timerSec;
    }
}
