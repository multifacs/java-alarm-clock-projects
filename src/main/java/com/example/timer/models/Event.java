package com.example.timer.models;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Event implements Serializable {
    // Поля класса
    public int id;

    public int hour;
    public int minute;
    public int second;

    public String name;

    public Event() {
        this.id = 0;
        this.hour = 0;
        this.minute = 0;
        this.second = 0;
        this.name = "New event";
    }

    public Event(int id, int hour, int minute, int second, String name) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.name = name;
    }

    // create formatter Object for ISO_LOCAL_TIME
    private static final DateTimeFormatter formatter
            = DateTimeFormatter.ISO_LOCAL_TIME;

    // Выводим информацию по продукту
    @Override
    public String toString() {
        LocalTime time = LocalTime.of(
                this.hour,
                this.minute,
                this.second);
        return String.format("ID: %s | Время: %s | Название: %s",
                this.id, time.format(formatter), this.name);
    }
}
