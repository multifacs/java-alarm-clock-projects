package com.example.timer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {

    private static final List<ObjectOutputStream> outputStreams = new ArrayList<>();
    private static final DB db = new DB();
    private static final Timer timer = new Timer();

    public static void main(String args[]) throws SQLException {

        db.setConnection();
        server();
        timer();
    }

    private static void server() {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(8888)) {
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
                                toClient.writeObject(db.getAll());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        while (true) {
                            Alarm alarm = null;
                            try {
                                alarm = (Alarm) fromClient.readObject();
                            } catch (IOException e) {
                                outputStreams.remove(toClient);
                                break;
                            } catch (ClassNotFoundException e) {
                                outputStreams.remove(toClient);
                                break;
                            }
                            alarm = new Alarm(timer.getDeciSec() + alarm.getDeciSec());
                            db.insert(alarm);

                            for (ObjectOutputStream stream : outputStreams) {
                                synchronized (stream) {
                                    try {
                                        stream.writeObject(2);
                                        stream.writeObject(db.getAll());
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

    private static void timer() {
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

                List<Alarm> eventList = db.getAll();

                for (Alarm event : eventList) {
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

                        db.delete(event);

                        for (ObjectOutputStream stream : outputStreams) {
                            synchronized (stream) {
                                try {
                                    stream.writeObject(2);
                                    stream.writeObject(db.getAll());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                timer.incTime();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.start();
    }
}

class Timer {

    private int deciSec;

    public Timer() {
        this.deciSec = 0;
    }

    public String getTimeString() {
        return String.format("%02d:" , this.deciSec / 10 / 60) +
                String.format("%02d." , this.deciSec / 10 % 60) +
                this.deciSec % 10;
    }

    public void incTime() {

        this.deciSec += 1;
    }

    public int getDeciSec() {
        return deciSec;
    }

    public boolean compare(Alarm event) {

        return event.getDeciSec() <= deciSec;
    }
}

class DB {
    private Connection connection;

    public void setConnection() {

        try {
            String url = "jdbc:h2:file:./ServerDB";
            connection = DriverManager.getConnection(url);
            var stmt = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS ALARMS (" +
                    " `decisecs` INTEGER" +
                    ");";
            stmt.executeUpdate(sql);
            System.out.println("connection successful");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Alarm> getAll() {

        // Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            // В данный список будем загружать наши продукты, полученные из БД
            List<Alarm> events = new ArrayList<Alarm>();
            // В resultSet будет храниться результат нашего запроса,
            // который выполняется командой statement.executeQuery()
            ResultSet resultSet = statement.executeQuery("SELECT `decisecs` FROM ALARMS");
            // Проходимся по нашему resultSet и заносим данные в products
            while (resultSet.next()) {
                events.add(new Alarm(resultSet.getInt("decisecs")));
            }
            // Возвращаем наш список
            return events;

        } catch (SQLException e) {
            System.out.println("DB closing");
            e.printStackTrace();
            // Если произошла ошибка - возвращаем пустую коллекцию
            return Collections.emptyList();
        }
    }

    public void insert(Alarm data) {

        // Создадим подготовленное выражение, чтобы избежать SQL-инъекций
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO ALARMS(`decisecs`) " +
                        "VALUES(?)")) {
            statement.setObject(1, data.getDeciSec());
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Alarm data) {

        try (PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM ALARMS WHERE decisecs = ?")) {
            statement.setObject(1, data.getDeciSec());
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}