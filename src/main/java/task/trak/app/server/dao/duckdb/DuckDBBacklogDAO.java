package task.trak.app.server.dao.duckdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.backlog.BackLog;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DuckDBBacklogDAO implements EntityDAO<BackLog> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    private static final Type LONG_LIST_TYPE = new TypeToken<List<Long>>() {}.getType();

    @Override
    public void save(BackLog entity) {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            String sql = "INSERT OR REPLACE INTO backlogs (id, name, project_name, task_ids, created_at) VALUES (?, ?, ?, ?, ?)";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, entity.getId());
                ps.setString(2, entity.getName());
                ps.setString(3, entity.getProject_name());
                ps.setString(4, GSON.toJson(entity.getTask_ids()));
                ps.setObject(5, entity.getCreated_at() != null ? entity.getCreated_at().getTime() : null);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save backlog: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public BackLog loadByKey(String key) {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            String sql = "SELECT * FROM backlogs WHERE name = ?";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return fromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load backlog: " + e.getMessage());
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
            String sql = "DELETE FROM backlogs WHERE name = ?";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to delete backlog: " + e.getMessage());
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<BackLog> loadAll() {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            List<BackLog> backlogs = new ArrayList<>();
            String sql = "SELECT * FROM backlogs";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    backlogs.add(fromResultSet(rs));
                }
            }
            return backlogs;
        } catch (SQLException e) {
            System.err.println("Failed to load all backlogs: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return new ArrayList<>();
    }

    private BackLog fromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String projectName = rs.getString("project_name");
        String taskIdsJson = rs.getString("task_ids");
        Long createdAtMs = rs.getObject("created_at") != null ? rs.getLong("created_at") : null;

        List<Long> taskIds = taskIdsJson != null ? GSON.fromJson(taskIdsJson, LONG_LIST_TYPE) : new ArrayList<>();
        Date createdAt = createdAtMs != null ? new Date(createdAtMs) : null;

        return new BackLog(id, name, projectName, taskIds, createdAt);
    }
}
