package com.example.timerserver.logic;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Scope("singleton")
public class Timer {
    private Instant currentTime;

    public Timer() {
        this.currentTime = Instant.now();
    }

    public Instant getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Instant currentTime) {
        this.currentTime = currentTime;
    }

    public void tick() {
        this.currentTime = this.currentTime.plus(1, ChronoUnit.SECONDS);
    }
}