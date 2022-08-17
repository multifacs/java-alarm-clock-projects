package com.example.timer.client;

import com.example.timer.data.EventData;
import com.example.timer.data.EventListData;
import com.example.timer.data.OpCodes;
import com.example.timer.data.TimeData;
import com.example.timer.models.Event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Client {

    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;
    TimeData time;
    EventListData events;
    EventData event;
    int timeout;
    LocalTime currentTime;
    final DateTimeFormatter formatter;

    public Client() {
        this.time = new TimeData();
        this.events = new EventListData();
        this.event = new EventData();
        this.timeout = 0;
        this.currentTime = LocalTime.now();
        this.formatter = DateTimeFormatter.ISO_LOCAL_TIME;
    }

    void receiveMessage(OpCodes opcode) throws IOException, ClassNotFoundException {
        switch (opcode) {
            case TIME -> time = (TimeData) fromServer.readObject();
            case EVENTS -> events = (EventListData) fromServer.readObject();
            case EVENT -> {
                event = (EventData) fromServer.readObject();
                timeout = 1;
            }
        }
    }
    void sendEvent(String hours, String minutes, String seconds, String eventName) {

        Event event = new Event();

        try {
            event.hour = Integer.parseInt(hours);
            event.minute = Integer.parseInt(minutes);
            event.second = Integer.parseInt(seconds);
            event.name = eventName;
        } catch (NumberFormatException e) {
            return;
        }

        if (event.hour < 0 || event.hour > 23) return;
        if (event.minute < 0 || event.minute > 59) return;
        if (event.second < 0 || event.second > 59) return;

        synchronized (toServer) {
            try {
                EventData eventData = new EventData(event);
                toServer.writeObject(eventData);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void start(int PORT) {

        Thread clientThread = new Thread(() -> {
            Socket socket;
            try {
                socket = new Socket(InetAddress.getLocalHost(), PORT);
                toServer = new ObjectOutputStream(socket.getOutputStream());
                fromServer = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    try {
                        OpCodes opcode = OpCodes.values()[(int) fromServer.readObject()];
                        receiveMessage(opcode);
                    } catch (IOException | ClassNotFoundException e) {
                        // e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                // e.printStackTrace();
            }
        });
        clientThread.setDaemon(true);
        clientThread.start();
    }
}