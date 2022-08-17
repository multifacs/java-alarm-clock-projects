package com.example.server;

import com.github.simplenet.Server;
import com.github.simplenet.packet.Packet;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.List;

public class ServerApp {

    private static Timer timer = new Timer();
    private static Database db = new Database();

    private static String formatSendString() {
        List<String> data;
        String parsedData = "";
        try {
            data = db.getAll();
            for (String row : data) {
                parsedData += row + " ";
            }
            return parsedData;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void sendTime(Server server) {
        server.queueAndFlushToAllExcept(Packet.builder().putByte(1).putString(timer.getTime().toString()));
    }

    private static void sendAllEvents(Server server) {
        server.queueAndFlushToAllExcept(Packet.builder().putByte(2).putString(formatSendString()));
    }

    private static void sendOccurredEvent(Server server, String row) {
        server.queueAndFlushToAllExcept(Packet.builder().putByte(3).putString(row));
    }

    private static boolean hasEventOccurred(int min, int sec) {
        return min == timer.getTime().atZone(ZoneId.systemDefault()).getMinute() && sec == timer.getTime().atZone(ZoneId.systemDefault()).getSecond();
    }

    public static void main(String[] args) {
        // Initialize a new server.
        var server = new Server();

        // This callback is invoked when a client connects to this server.
        server.onConnect(client -> {

            Thread clientThread = new Thread(() -> {
                sendAllEvents(server);

                client.readStringAlways(message -> {
                    System.out.println(message);

                    try {
                        db.insert(message);
                        sendAllEvents(server);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            });

            clientThread.start();
        });

        Thread serviceThread = new Thread(() -> {
            db.setConnection();

            while(true) {
                timer.tick();

                sendTime(server);

                List<String> data = null;
                try {
                    data = db.getAll();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                for (String row : data) {
                    int min = Integer.parseInt(row.substring(0, 2));
                    int sec = Integer.parseInt(row.substring(3));

                    if (hasEventOccurred(min, sec)) {
                        sendOccurredEvent(server, row);
                        try {
                            db.delete(row);
                            sendAllEvents(server);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        serviceThread.start();

        // Bind the server to our local network AFTER registering listeners.
        server.bind("localhost", 43594);
    }
}
