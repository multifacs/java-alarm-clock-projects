package com.example.timer.server;

import com.example.timer.data.EventData;
import com.example.timer.data.EventListData;
import com.example.timer.data.OpCodes;
import com.example.timer.data.TimeData;
import com.example.timer.models.Event;
import com.example.timer.server.utils.ClientConnection;
import com.example.timer.server.utils.DatabaseHandler;
import com.example.timer.server.utils.Timer;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {

    final DatabaseHandler db = new DatabaseHandler();
    final Timer timer = new Timer();
    private final List<ClientConnection> connectionList = Collections.synchronizedList(new ArrayList<>());

    private void listen(int PORT) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {

                ClientConnection client = new ClientConnection(server.accept());
                connectionList.add(client);
                startClientThread(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void startClientThread(ClientConnection client) {

        Thread clientThread = new Thread(() -> {

            sendEvents(client);
            listenForClientEvent(client);

            connectionList.remove(client);
        });
        clientThread.start();
    }
    private void listenForClientEvent(ClientConnection client) {

        while (true) {

            EventData event = (EventData) client.receiveMessage();

            if (event == null) {
                break;
            }

            db.insert(event.getEvent());
            broadcastEvents();
        }
    }
    private void startTimer() {

        Thread timerThread = new Thread(() -> {

            while (true) {

                broadcastTime();
                checkEvents();

                System.out.println(timer.getTime());
                timer.tick();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.start();
    }

    private void checkEvents() {

        List<Event> data = null;
        data = db.getAll();
        assert data != null;

        for (Event event : data) {
            if (eventHappened(
                    event.hour,
                    event.minute,
                    event.second
            )) {
                broadcastEvent(event);
                db.delete(event);
                broadcastEvents();
            }
        }
    }
    private boolean eventHappened(int hour, int min, int sec) {

        LocalTime eventTime = LocalTime.of(hour, min, sec);
        return (eventTime.compareTo(timer.getTime()) < 0);
    }

    private void broadcast(OpCodes opcode, Object s) {

        for (ClientConnection client : connectionList) {
            client.sendMessage(opcode, s);
        }
    }
    private void broadcastTime() {

        TimeData time = new TimeData(timer.getTime());
        broadcast(OpCodes.TIME, time);
    }
    private void broadcastEvents() {

        EventListData list = new EventListData(db.getAll());
        broadcast(OpCodes.EVENTS, list);
    }
    private void broadcastEvent(Event event) {

        EventData eventData = new EventData(event);
        broadcast(OpCodes.EVENT, eventData);
    }
    private void sendEvents(ClientConnection client) {

        EventListData list = new EventListData(db.getAll());
        client.sendMessage(OpCodes.EVENTS, list);
    }

    public void start(int PORT) {

        db.setConnection();

        startTimer();
        listen(PORT);
    }
}
