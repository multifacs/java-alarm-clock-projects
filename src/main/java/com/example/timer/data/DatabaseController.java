package com.example.timer.data;

import org.sqlite.JDBC;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;

public class DatabaseController {

    private static final String CON_STR = "jdbc:sqlite:./myfin.db";
    private static DatabaseController instance = null;

    public static synchronized DatabaseController getInstance() throws SQLException {
        if (instance == null)
            instance = new DatabaseController();
        return instance;
    }

    private final Connection connection;

    private DatabaseController() throws SQLException {
        DriverManager.registerDriver(new JDBC());
        this.connection = DriverManager.getConnection(CON_STR);
    }

    public List<Event> getAllEvents() {

        try (Statement statement = this.connection.createStatement()) {
            List<Event> events = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery("SELECT name, min, sec FROM Events");
            while (resultSet.next()) {
                events.add(new Event(
                        resultSet.getString("name"),
                        resultSet.getInt("min"),
                        resultSet.getInt("sec")
                ));
            }
            return events;

        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void addEvent(Event event) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Events(name, min, sec) " +
                        "VALUES(?, ?, ?)")) {
            statement.setObject(1, event.getName());
            statement.setObject(2, event.getMin());
            statement.setObject(3, event.getSec());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEvent(Event event) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM Events WHERE name = ?" +
                        " AND min = ?" +
                        " AND sec = ?")) {
            statement.setObject(1, event.getName());
            statement.setObject(2, event.getMin());
            statement.setObject(3, event.getSec());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}