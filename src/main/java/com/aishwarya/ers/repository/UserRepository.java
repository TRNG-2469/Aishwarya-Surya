package com.aishwarya.ers.repository;

import com.aishwarya.ers.model.Role;
import com.aishwarya.ers.model.User;
import com.aishwarya.ers.util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public boolean createUser(User user) {

        String sql = """
            INSERT INTO users (username, password_hash, role, department)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name());
            statement.setString(4, user.getDepartment());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected != 1) {
                return false;
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }

            return true;

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

    public List<User> findAll() {

        String sql = """
                SELECT id, username, password_hash, role, department
                FROM users
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    users.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public boolean update(User user) {

        String sql = """
                UPDATE users
                SET username = ?, password_hash = ?, role = ?, department = ?
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name());
            statement.setString(4, user.getDepartment());
            statement.setInt(5, user.getId());

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {

        String sql = """
                DELETE FROM users
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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