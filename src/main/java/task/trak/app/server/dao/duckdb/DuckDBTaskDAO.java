package task.trak.app.server.dao.duckdb;

import task.trak.api.service.STATE;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.task.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DuckDBTaskDAO implements EntityDAO<Task> {

    @Override
    public void save(Task entity) {
        String sql = "INSERT OR REPLACE INTO tasks (id, project_name, assigned_to, title, status, created_at, completed_at, summary, deadline, estimate, time_started, time_spent_ms) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, entity.getId());
                ps.setString(2, entity.getProject_name());
                ps.setString(3, entity.getAssigned_to());
                ps.setString(4, entity.getTitle());
                ps.setString(5, entity.getStatus() != null ? entity.getStatus().name() : null);
                ps.setObject(6, entity.getCreated_at() != null ? entity.getCreated_at().getTime() : null);
                ps.setObject(7, entity.getCompleted_at() != null ? entity.getCompleted_at().getTime() : null);
                ps.setString(8, entity.getSummary());
                ps.setObject(9, entity.getDeadline() != null ? entity.getDeadline().getTime() : null);
                ps.setString(10, entity.getEstimate());
                ps.setObject(11, entity.getTime_started());
                ps.setObject(12, entity.getTime_spent_ms());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save task: " + e.getMessage());
        }
    }

    @Override
    public Task loadByKey(String key) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try {
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, Long.parseLong(key));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return fromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load task: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean deleteByKey(String key) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try {
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, Long.parseLong(key));
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to delete task: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Task> loadAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try {
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load all tasks: " + e.getMessage());
        }
        return tasks;
    }

    private Task fromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String projectName = rs.getString("project_name");
        String assignedTo = rs.getString("assigned_to");
        String title = rs.getString("title");
        String statusStr = rs.getString("status");
        STATE status = statusStr != null ? STATE.valueOf(statusStr) : STATE.READY;
        Long createdAtMs = rs.getObject("created_at") != null ? rs.getLong("created_at") : null;
        Long completedAtMs = rs.getObject("completed_at") != null ? rs.getLong("completed_at") : null;
        Date createdAt = createdAtMs != null ? new Date(createdAtMs) : null;
        Date completedAt = completedAtMs != null ? new Date(completedAtMs) : null;
        String summary = rs.getString("summary");

        Task task = new Task(id, projectName, assignedTo, title, status, createdAt, completedAt, summary);

        Long deadlineMs = rs.getObject("deadline") != null ? rs.getLong("deadline") : null;
        if (deadlineMs != null) task.setDeadline(new Date(deadlineMs));
        task.setEstimate(rs.getString("estimate"));
        task.setTime_started(rs.getObject("time_started") != null ? rs.getLong("time_started") : null);
        task.setTime_spent_ms(rs.getObject("time_spent_ms") != null ? rs.getLong("time_spent_ms") : null);

        return task;
    }
}
