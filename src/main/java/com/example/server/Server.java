package com.example.server;

import com.github.simplenet.packet.Packet;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Server {

    private static final Timer timer = new Timer();
    private static final Database db = new Database();

    private static String formatSendString() {
        List<String> data;
        StringBuilder parsedData = new StringBuilder();
        try {
            data = db.all();
            for (String row : data) {
                parsedData.append(row).append(" ");
            }
            return parsedData.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void sendTime(com.github.simplenet.Server server) {
        server.queueAndFlushToAllExcept(Packet.builder().putByte(1).putString(timer.getTime().toString()));
    }

    private static void sendEvents(com.github.simplenet.Server server) {
        server.queueAndFlushToAllExcept(Packet.builder().putByte(2).putString(formatSendString()));
    }

    private static void sendEvent(com.github.simplenet.Server server, String row) {
        server.queueAndFlushToAllExcept(Packet.builder().putByte(3).putString(row));
    }

    private static List<Integer> parseTime(String row) {
        int hrs = Integer.parseInt(row.substring(0, 2));
        int min = Integer.parseInt(row.substring(3, 5));
        int sec = Integer.parseInt(row.substring(6));

        return Arrays.asList(hrs, min, sec);
    }

    private static boolean hasEventHappened(int hrs, int min, int sec) {
        return hrs <= timer.getTime().getHour() && min <= timer.getTime().getMinute() && sec <= timer.getTime().getSecond();
    }

    private static void checkEvent(int hrs, int min, int sec, String row, com.github.simplenet.Server server) {
        if (hasEventHappened(hrs, min, sec)) {
            sendEvent(server, row);
            try {
                db.remove(row);
                sendEvents(server);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        var server = new com.github.simplenet.Server();

        server.onConnect(client -> {

            Thread clientThread = new Thread(() -> {
                sendEvents(server);

                client.readStringAlways(message -> {
                    System.out.println(message);

                    try {
                        db.add(message);
                        sendEvents(server);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            });

            clientThread.start();
        });

        Thread serviceThread = new Thread(() -> {
            db.connect();

            while(true) {
                timer.tick();

                sendTime(server);

                List<String> data = null;
                try {
                    data = db.all();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                assert data != null;
                for (String event : data) {
                    List<Integer> parsedTime = parseTime(event);
                    int hrs = parsedTime.get(0);
                    int min = parsedTime.get(1);
                    int sec = parsedTime.get(2);

                    checkEvent(hrs, min, sec, event, server);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        serviceThread.start();

        server.bind("localhost", 43594);
    }
}
