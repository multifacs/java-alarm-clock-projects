package com.example.timer.server.utils;

import com.example.timer.models.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseHandler {
    private Connection connection;

    public void setConnection() {

        try {
            String url = "jdbc:h2:file:./ServerDB";
            connection = DriverManager.getConnection(url);
            var stmt = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS EVENTS (" +
                    "`id` INTEGER NOT NULL AUTO_INCREMENT," +
                    " `hour` INTEGER," +
                    " `minute` INTEGER," +
                    " `second` INTEGER," +
                    " `name` VARCHAR(255)" +
                    ");";
            stmt.executeUpdate(sql);
            System.out.println("connection successful");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Event> getAll() {

        // Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            // В данный список будем загружать наши продукты, полученные из БД
            List<Event> events = new ArrayList<Event>();
            // В resultSet будет храниться результат нашего запроса,
            // который выполняется командой statement.executeQuery()
            ResultSet resultSet = statement.executeQuery("SELECT `id`, `hour`, `minute`, `second`, `name` FROM Events");
            // Проходимся по нашему resultSet и заносим данные в products
            while (resultSet.next()) {
                events.add(new Event(resultSet.getInt("id"),
                        resultSet.getInt("hour"),
                        resultSet.getInt("minute"),
                        resultSet.getInt("second"),
                        resultSet.getString("name")));
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

    public void insert(Event data) {

        // Создадим подготовленное выражение, чтобы избежать SQL-инъекций
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Events(`hour`, `minute`, `second`, `name`) " +
                        "VALUES(?, ?, ?, ?)")) {
            statement.setObject(1, data.hour);
            statement.setObject(2, data.minute);
            statement.setObject(3, data.second);
            statement.setObject(4, data.name);
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Event data) {

        try (PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM Events WHERE id = ?")) {
            statement.setObject(1, data.id);
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}