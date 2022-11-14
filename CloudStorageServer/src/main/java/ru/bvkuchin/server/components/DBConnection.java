package ru.bvkuchin.server.components;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final String url = "jdbc:sqlite:/home/boris/geekbrains/CloudStorage/CloudStorageServer/src/main/resources/db/users.db";
    @Getter
    private Connection connection;


    public DBConnection() {

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
