package com.example.timer.data;

import com.example.timer.models.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventListData implements Serializable {
    private List<Event> events;

    public EventListData() {
        this.events = new ArrayList<>();
    }

    public EventListData(List<Event> events) {
        this.events = events;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
