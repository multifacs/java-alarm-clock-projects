package com.example.timer;

import org.sqlite.JDBC;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;

public class Database {

    // Константа, в которой хранится адрес подключения
    private static final String CON_STR = "jdbc:sqlite:./myfin.db";

    // Используем шаблон одиночка, чтобы не плодить множество
    // экземпляров класса Database
    private static Database instance = null;

    public static synchronized Database getInstance() throws SQLException {
        if (instance == null)
            instance = new Database();
        return instance;
    }

    // Объект, в котором будет храниться соединение с БД
    private Connection connection;

    private Database() throws SQLException {
        // Регистрируем драйвер, с которым будем работать
        // в нашем случае Sqlite
        DriverManager.registerDriver(new JDBC());
        // Выполняем подключение к базе данных
        this.connection = DriverManager.getConnection(CON_STR);
    }

    public List<Event> getAllEvents() {

        // Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            // В данный список будем загружать наши продукты, полученные из БД
            List<Event> events = new ArrayList<>();
            // В resultSet будет храниться результат нашего запроса,
            // который выполняется командой statement.executeQuery()
            ResultSet resultSet = statement.executeQuery("SELECT time FROM Events");
            // Проходимся по нашему resultSet и заносим данные в products
            while (resultSet.next()) {
                events.add(new Event(LocalTime.parse(resultSet.getString("time"))));
            }
            // Возвращаем наш список
            return events;

        } catch (SQLException e) {
            e.printStackTrace();
            // Если произошла ошибка - возвращаем пустую коллекцию
            return Collections.emptyList();
        }
    }

    // Добавление продукта в БД
    public void addEvent(Event event) {
        // Создадим подготовленное выражение, чтобы избежать SQL-инъекций
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Events(`time`) " +
                        "VALUES(?)")) {
            statement.setObject(1, event.getTime().toString());
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Удаление продукта по id
    public void deleteEvent(String time) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM Events WHERE time = ?")) {
            statement.setObject(1, time);
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}