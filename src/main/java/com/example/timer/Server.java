package com.example.timer;

import com.example.timer.data.DatabaseController;
import com.example.timer.data.Event;
import com.example.timer.data.Timer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final List<ObjectOutputStream> outputStreams = new ArrayList<>();
    private static DatabaseController databaseConnection;
    private static final Timer timer = new Timer();

    public static void main(String args[]) throws SQLException {
        final int port = 8888;
        databaseConnection = DatabaseController.getInstance();
        startServer(port);
        startTimer();
    }

    private static void startServer(int port) {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                while (true) {
                    Socket socket = server.accept();
                    ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());

                    Thread clientThread = new Thread(() -> {

                        outputStreams.add(toClient);
                        System.out.println(outputStreams);

                        synchronized (toClient) {
                            try {
                                toClient.writeObject(2);
                                toClient.writeObject(databaseConnection.getAllEvents());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        while (true) {
                            Event event = null;
                            try {
                                event = (Event) fromClient.readObject();
                            } catch (IOException e) {
                                outputStreams.remove(toClient);
                                break;
                            } catch (ClassNotFoundException e) {
                                outputStreams.remove(toClient);
                                break;
                            }
                            databaseConnection.addEvent(event);

                            for (ObjectOutputStream stream : outputStreams) {
                                synchronized (stream) {
                                    try {
                                        stream.writeObject(2);
                                        stream.writeObject(databaseConnection.getAllEvents());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                    clientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    private static void startTimer() {
        Thread timerThread = new Thread(() -> {
            while (true) {

                for (ObjectOutputStream stream : outputStreams) {
                    synchronized (stream) {
                        try {
                            stream.writeObject(1);
                            stream.writeObject(timer.getTimeString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                List<Event> eventList = databaseConnection.getAllEvents();

                for (Event event : eventList) {
                    if (timer.compare(event)) {

                        for (ObjectOutputStream stream : outputStreams) {
                            synchronized (stream) {
                                try {
                                    stream.writeObject(3);
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

                        databaseConnection.deleteEvent(event);

                        for (ObjectOutputStream stream : outputStreams) {
                            synchronized (stream) {
                                try {
                                    stream.writeObject(2);
                                    stream.writeObject(databaseConnection.getAllEvents());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                timer.incTime();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.start();
    }
}

