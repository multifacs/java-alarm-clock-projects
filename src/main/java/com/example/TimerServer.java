package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class TimerServer extends WebSocketServer {

    DataBase db;
    Timer timer;

    public TimerServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        db = new DataBase();
        timer = new Timer();
    }

    public TimerServer(InetSocketAddress address) {
        super(address);
    }

    public TimerServer(int port, Draft_6455 draft) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println(
                conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected!");
        Thread onOpenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                conn.send(sendAll());
            }
        });
        onOpenThread.start();
    }

    String sendAll() {
        List<String> data = null;
        String parsedData = "";
        try {
            data = db.getAll();
            for (String row : data) {
                parsedData += row + "\n";
            }
            return parsedData;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(conn + " disconnected!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // broadcast(message);
        Thread onMsgThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    db.insert(message);
                    broadcast(sendAll());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        onMsgThread.start();

        System.out.println(conn + ": " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        // broadcast(message.array());
        System.out.println(conn + ": " + message);
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 8887;
        TimerServer s = new TimerServer(port);
        s.start();
        System.out.println("Timer app started on port: " + s.getPort());

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            s.broadcast(in);
            if (in.equals("exit")) {
                s.stop(1000);
                break;
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);

        db.setConnection();

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    List<String> data = null;
                    try {
                        data = db.getAll();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    for (String row : data) {
                        int min = Integer.parseInt(row.substring(0, 2));
                        int sec = Integer.parseInt(row.substring(3));
                        System.out.println(min + " " + sec);
                        if (min == timer.getTime().atZone(ZoneId.systemDefault()).getMinute() && sec == timer.getTime().atZone(ZoneId.systemDefault()).getSecond()) {
                            broadcast("event " + "Событие " + row);
                            try {
                                db.delete(row);
                                broadcast(sendAll());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    broadcast("time " + timer.getTime().toString());
                    timer.plus();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        serverThread.start();
    }

}

class DataBase {
    String url = "jdbc:h2:mem:";
    Connection connection;

    public void setConnection() {
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("DB connected");

            System.out.println("Creating table in database...");
            var stmt = connection.createStatement();
            String sql = "CREATE TABLE   EVENTS " +
                    "(time VARCHAR(255))";
            stmt.executeUpdate(sql);
            System.out.println("Created table in database...");

            insert("10:00");
            insert("11:00");
            insert("12:00");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAll() throws SQLException {
        List<String> data = new ArrayList<>();
        String sql = "SELECT time FROM Events";
        var stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            String time = rs.getString("time");
            data.add(time);
        }
        rs.close();
        System.out.println(data);
        return data;
    }

    public void insert(String data) throws SQLException {
        String sql = "INSERT INTO Events " + "VALUES ('" + data + "')";
        var stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public void delete(String data) throws SQLException {
        String sql = "DELETE FROM Events " + "WHERE time = '" + data + "'";
        var stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }
}

class Timer {
    Instant time;

    public Timer() {
        time = Instant.now();
    }

    public Instant getTime() {
        return time;
    }

    public void plus() {
        time = time.plus(1, ChronoUnit.SECONDS);
    }
}