package com.aishwarya.ers.util;

import java.sql.Connection;

public class ConnectionTest {

    public static void main(String[] args) {

        try (Connection connection = ConnectionFactory.getConnection()) {

            if (connection != null) {
                System.out.println("Connected to PostgreSQL successfully!");
            }

        } catch (Exception e) {
            e.getMessage();
        }
    }
}