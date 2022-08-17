package com.example.timer.data;

import com.example.timer.models.Event;

import java.io.Serializable;

public class EventData implements Serializable {
    private Event event;

    public EventData() {
        this.event = new Event();
    }
    public EventData(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
