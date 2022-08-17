package com.example.timer;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Timer {
    LocalTime time;

    public Timer() {
        this.time = LocalTime.now();
    }

    public LocalTime getTime() {
        return time;
    }

    public void timerPlus() {
        time = time.plus(1, ChronoUnit.SECONDS);
    }
}
