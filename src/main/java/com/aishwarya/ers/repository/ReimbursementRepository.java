package com.aishwarya.ers.repository;

import com.aishwarya.ers.model.Reimbursement;
import com.aishwarya.ers.model.ReimbursementStatus;
import com.aishwarya.ers.model.ReimbursementType;
import com.aishwarya.ers.util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReimbursementRepository {

    public boolean create(Reimbursement reimbursement) {

        String sql = """
                INSERT INTO reimbursements
                (user_id, amount, description, type, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setInt(1, reimbursement.getUserId());
            statement.setBigDecimal(2, reimbursement.getAmount());
            statement.setString(3, reimbursement.getDescription());
            statement.setString(4, reimbursement.getType().name());
            statement.setString(5, reimbursement.getStatus().name());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted == 1) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reimbursement.setId(generatedKeys.getInt(1));
                    }
                }

                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Reimbursement findById(int id) {

        String sql = """
                SELECT id, user_id, amount, description, type, status,
                       resolver_id, created_at, resolved_at
                FROM reimbursements
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

    public List<Reimbursement> findByUserId(int userId) {

        String sql = """
                SELECT id, user_id, amount, description, type, status,
                       resolver_id, created_at, resolved_at
                FROM reimbursements
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;

        List<Reimbursement> reimbursements = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reimbursements.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reimbursements;
    }

    public List<Reimbursement> findAll() {

        String sql = """
                SELECT id, user_id, amount, description, type, status,
                       resolver_id, created_at, resolved_at
                FROM reimbursements
                ORDER BY created_at DESC
                """;

        List<Reimbursement> reimbursements = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reimbursements.add(mapRow(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reimbursements;
    }

    public List<Reimbursement> findByStatus(ReimbursementStatus status) {

        String sql = """
                SELECT id, user_id, amount, description, type, status,
                       resolver_id, created_at, resolved_at
                FROM reimbursements
                WHERE status = ?
                ORDER BY created_at DESC
                """;

        List<Reimbursement> reimbursements = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reimbursements.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reimbursements;
    }

    public boolean updatePending(Reimbursement reimbursement) {

        String sql = """
                UPDATE reimbursements
                SET amount = ?,
                    description = ?,
                    type = ?
                WHERE id = ?
                  AND user_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, reimbursement.getAmount());
            statement.setString(2, reimbursement.getDescription());
            statement.setString(3, reimbursement.getType().name());
            statement.setInt(4, reimbursement.getId());
            statement.setInt(5, reimbursement.getUserId());

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean resolve(
            int reimbursementId,
            ReimbursementStatus status,
            int resolverId
    ) {

        String sql = """
                UPDATE reimbursements
                SET status = ?,
                    resolver_id = ?,
                    resolved_at = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setInt(2, resolverId);
            statement.setInt(3, reimbursementId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Reimbursement mapRow(ResultSet resultSet) throws SQLException {

        Reimbursement reimbursement = new Reimbursement();

        reimbursement.setId(resultSet.getInt("id"));
        reimbursement.setUserId(resultSet.getInt("user_id"));
        reimbursement.setAmount(resultSet.getBigDecimal("amount"));
        reimbursement.setDescription(resultSet.getString("description"));
        reimbursement.setType(
                ReimbursementType.valueOf(resultSet.getString("type"))
        );
        reimbursement.setStatus(
                ReimbursementStatus.valueOf(resultSet.getString("status"))
        );

        int resolverId = resultSet.getInt("resolver_id");

        if (resultSet.wasNull()) {
            reimbursement.setResolverId(null);
        } else {
            reimbursement.setResolverId(resolverId);
        }

        Timestamp createdAt = resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            reimbursement.setCreatedAt(createdAt.toLocalDateTime());
        }

        return reimbursement;
    }

    public List<Reimbursement> findByFilters(ReimbursementStatus status, String department) {

        StringBuilder sql = new StringBuilder("""
            SELECT r.id, r.user_id, r.amount, r.description, r.type, r.status,
                   r.resolver_id, r.created_at, r.resolved_at
            FROM reimbursements r
            JOIN users u ON r.user_id = u.id
            WHERE 1=1
            """);

        List<String> params = new ArrayList<>();

        if (status != null) {
            sql.append(" AND r.status = ?");
            params.add(status.name());
        }

        if (department != null) {
            sql.append(" AND u.department = ?");
            params.add(department);
        }

        sql.append(" ORDER BY r.created_at DESC");

        List<Reimbursement> reimbursements = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setString(i + 1, params.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reimbursements.add(mapRow(resultSet));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reimbursements;
    }

}
