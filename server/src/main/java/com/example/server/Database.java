package com.example.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Database {
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

            insert("15:30");

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
