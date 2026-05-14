package task.trak.app.server.dao.duckdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.sprint.Sprint;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DuckDBSprintDAO implements EntityDAO<Sprint> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    private static final Type LONG_LIST_TYPE = new TypeToken<List<Long>>() {}.getType();

    @Override
    public void save(Sprint entity) {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            String sql = "INSERT OR REPLACE INTO sprints (id, project_name, name, task_ids, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, entity.getId());
                ps.setString(2, entity.getProject_name());
                ps.setString(3, entity.getName());
                ps.setString(4, GSON.toJson(entity.getTask_ids()));
                ps.setObject(5, entity.getStart_date() != null ? entity.getStart_date().getTime() : null);
                ps.setObject(6, entity.getEnd_date() != null ? entity.getEnd_date().getTime() : null);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save sprint: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Sprint loadByKey(String key) {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            Connection conn = DuckDBConnection.getConnection();
            // Try by ID first, fall back to name
            try {
                long id = Long.parseLong(key);
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM sprints WHERE id = ?")) {
                    ps.setLong(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) return fromResultSet(rs);
                    }
                }
            } catch (NumberFormatException ignored) {}
            // Fall back to name lookup
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM sprints WHERE name = ?")) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return fromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load sprint: " + e.getMessage());
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
            Connection conn = DuckDBConnection.getConnection();
            // Try by ID first, fall back to name
            try {
                long id = Long.parseLong(key);
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sprints WHERE id = ?")) {
                    ps.setLong(1, id);
                    if (ps.executeUpdate() > 0) return true;
                }
            } catch (NumberFormatException ignored) {}
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sprints WHERE name = ?")) {
                ps.setString(1, key);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to delete sprint: " + e.getMessage());
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Sprint> loadAll() {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            List<Sprint> sprints = new ArrayList<>();
            String sql = "SELECT * FROM sprints";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sprints.add(fromResultSet(rs));
                }
            }
            return sprints;
        } catch (SQLException e) {
            System.err.println("Failed to load all sprints: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return new ArrayList<>();
    }

    private Sprint fromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String projectName = rs.getString("project_name");
        String name = rs.getString("name");
        String taskIdsJson = rs.getString("task_ids");
        Long startDateMs = rs.getObject("start_date") != null ? rs.getLong("start_date") : null;
        Long endDateMs = rs.getObject("end_date") != null ? rs.getLong("end_date") : null;

        List<Long> taskIds = taskIdsJson != null ? GSON.fromJson(taskIdsJson, LONG_LIST_TYPE) : new ArrayList<>();
        Date startDate = startDateMs != null ? new Date(startDateMs) : null;
        Date endDate = endDateMs != null ? new Date(endDateMs) : null;

        return new Sprint(id, projectName, name, taskIds, startDate, endDate);
    }
}
