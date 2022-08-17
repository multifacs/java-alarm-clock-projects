package com.example.timer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final List<ObjectOutputStream> outputs = new ArrayList<>();
    private static Database database;
    private static final Timer timer = new Timer();

    private static final int TIME = 1;
    private static final int EVENTS = 2;
    private static final int NOTIFICATION = 3;

    public static void main(String args[]) throws SQLException {
        database = Database.getInstance();

        Thread timerThread = new Thread(() -> {
            while (true) {
                System.out.println(timer.getTime());
                sendTimeToAll();
                List<Event> eventList = database.getAllEvents();

                for (Event event : eventList) {
                    if (event.getTime().compareTo(timer.getTime()) < 0) {
                        sendNotificationToALl(event);
                        database.deleteEvent(event.getTime().toString());
                        sendEventsToAll();
                    }
                }

                timer.timerPlus();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.start();

        try (ServerSocket server = new ServerSocket(8888)) {
            while (true) {
                Socket socket = server.accept();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                Thread clientThread = new Thread(() -> {

                    outputs.add(output);
                    System.out.println(outputs);

                    synchronized (output) {
                        sendEvents(output);
                    }

                    while (true) {
                        Event event = null;
                        try {
                            event = (Event) input.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            outputs.remove(output);
                            break;
                        }
                        assert event != null;
                        database.addEvent(event);

                        sendEventsToAll();
                    }
                });
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendTimeToAll() {

        for (ObjectOutputStream stream : outputs) {
            synchronized (stream) {
                try {
                    stream.writeObject(TIME);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    stream.writeObject(timer.getTime().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static void sendEvents(ObjectOutputStream toClient) {
        try {
            toClient.writeObject(EVENTS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            toClient.writeObject(database.getAllEvents());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void sendEventsToAll() {

        for (ObjectOutputStream stream : outputs) {
            synchronized (stream) {
                sendEvents(stream);
            }
        }
    }
    private static void sendNotificationToALl(Event event) {

        for (ObjectOutputStream stream : outputs) {
            synchronized (stream) {
                try {
                    stream.writeObject(NOTIFICATION);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    stream.writeObject(event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

