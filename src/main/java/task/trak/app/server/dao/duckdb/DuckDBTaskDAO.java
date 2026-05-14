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
import java.util.concurrent.locks.ReentrantLock;

public class DuckDBTaskDAO implements EntityDAO<Task> {

    @Override
    public void save(Task entity) {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            String sql = "INSERT OR REPLACE INTO tasks (id, project_name, assigned_to, title, status, created_at, completed_at, summary, deadline, estimate, time_started, time_spent_ms, completion_note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                ps.setString(13, entity.getCompletion_note());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save task: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Task loadByKey(String key) {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            String sql = "SELECT * FROM tasks WHERE id = ?";
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
        } finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public boolean deleteByKey(String key) {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            String sql = "DELETE FROM tasks WHERE id = ?";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, Long.parseLong(key));
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to delete task: " + e.getMessage());
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Task> loadAll() {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            List<Task> tasks = new ArrayList<>();
            String sql = "SELECT * FROM tasks";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(fromResultSet(rs));
                }
            }
            return tasks;
        } catch (SQLException e) {
            System.err.println("Failed to load all tasks: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return new ArrayList<>();
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
        task.setCompletion_note(rs.getString("completion_note"));

        return task;
    }
}
