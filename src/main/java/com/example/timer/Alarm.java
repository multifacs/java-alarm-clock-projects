package com.example.timer;

import java.io.Serializable;

public class Alarm implements Serializable {

    private final int deciSec;

    public Alarm(int sec) {
        this.deciSec = sec;
    }

    public int getDeciSec() {
        return deciSec;
    }
}
