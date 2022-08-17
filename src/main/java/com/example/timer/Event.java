package com.example.timer;

import java.io.Serializable;
import java.time.LocalTime;

public class Event implements Serializable {
    private LocalTime time;

    public Event(LocalTime time) {
        this.time = time;
    }

    public LocalTime getTime() {
        return time;
    }
}
