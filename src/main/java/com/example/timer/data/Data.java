package com.example.timer.data;

import java.util.ArrayList;
import java.util.List;

public class Data {
    private String time;
    private List<Event> eventList;
    private Event currentEvent;

    public Data() {
        time = "";
        eventList = new ArrayList<>();
        currentEvent = null;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }
}
