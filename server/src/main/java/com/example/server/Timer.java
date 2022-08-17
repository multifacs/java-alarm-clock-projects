package com.example.server;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Timer {

    Instant time;

    public Timer() {
        this.time = Instant.now();
    }

    public Instant getTime() {
        return time;
    }

    public void tick() {
        time = time.plus(1, ChronoUnit.SECONDS);
    }
}
