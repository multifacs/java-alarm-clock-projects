package com.example.timerserver.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Event {
    @Id
    private String id;

    private Instant time;

    public Event(String id, Instant time) {
        this.id = id;
        this.time = time;
    }

    public Event() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
