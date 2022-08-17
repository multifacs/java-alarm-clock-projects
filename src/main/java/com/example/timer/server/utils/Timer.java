package com.example.timer.server.utils;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Timer {
    private LocalTime time;

    public Timer() {
        this.time = LocalTime.now();
    }

    public LocalTime getTime() {
        return time;
    }

    public void tick() {
        time = time.plus(1, ChronoUnit.SECONDS);
    }
}