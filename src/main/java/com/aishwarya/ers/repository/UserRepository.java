package com.aishwarya.ers.repository;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    public boolean createUser(User user) {

        String sql = """
                INSERT INTO users (username, password_hash, role, department)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name());
            statement.setString(4, user.getDepartment());

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findByUsername(String username) {

        String sql = """
                SELECT id, username, password_hash, role, department
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User findById(int id) {

        String sql = """
                SELECT id, username, password_hash, role, department
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private User mapRow(ResultSet resultSet) throws SQLException {

        User user = new User();

        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(Role.valueOf(resultSet.getString("role")));
        user.setDepartment(resultSet.getString("department"));

        return user;
    }
}
