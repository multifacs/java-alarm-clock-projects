package com.example.server;

import java.time.LocalTime;

public class Timer {

    LocalTime time;

    public Timer() {
        this.time = LocalTime.of(0, 0, 0);
    }

    public LocalTime getTime() {
        return time;
    }

    public void tick() {
        time = time.plusSeconds(1);
        System.out.println(time);
    }
}
