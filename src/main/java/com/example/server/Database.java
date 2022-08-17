package com.example.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Database {
    Connection connection;

    public void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:h2:file:./ServerDB");
            var stmt = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS EVENTS " +
                    "(time VARCHAR(255))";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> all() throws SQLException {

        // Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            // В данный список будем загружать наши продукты, полученные из БД
            List<String> events = new ArrayList<String>();
            // В resultSet будет храниться результат нашего запроса,
            // который выполняется командой statement.executeQuery()
            ResultSet resultSet = statement.executeQuery("SELECT time FROM Events");
            // Проходимся по нашему resultSet и заносим данные в products
            while (resultSet.next()) {
                events.add(resultSet.getString("time"));
            }
            // Возвращаем наш список
            return events;

        } catch (SQLException e) {
            e.printStackTrace();
            // Если произошла ошибка - возвращаем пустую коллекцию
            return Collections.emptyList();
        }
    }

    public void add(String data) throws SQLException {

        // Создадим подготовленное выражение, чтобы избежать SQL-инъекций
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Events(`time`) " +
                        "VALUES(?)")) {
            statement.setObject(1, data);
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(String data) throws SQLException {

        try (PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM Events WHERE time = ?")) {
            statement.setObject(1, data);
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
