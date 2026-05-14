package task.trak.app.server.dao.duckdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.user.User;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DuckDBUserDAO implements EntityDAO<User> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    private static final Type LONG_LIST_TYPE = new TypeToken<List<Long>>() {}.getType();

    @Override
    public void save(User entity) {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            String sql = "INSERT OR REPLACE INTO users (user_name, id, first_name, last_name, email, password_hash, tasks, projects) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entity.getUser_name());
                ps.setObject(2, entity.getID());
                ps.setString(3, entity.getFirst_name());
                ps.setString(4, entity.getLast_name());
                ps.setString(5, entity.getEmail());
                ps.setString(6, entity.getPassword_hash());
                ps.setString(7, GSON.toJson(entity.getTasks()));
                ps.setString(8, GSON.toJson(entity.getProjects()));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save user: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public User loadByKey(String key) {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            String sql = "SELECT * FROM users WHERE user_name = ?";
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
            System.err.println("Failed to load user: " + e.getMessage());
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
            String sql = "DELETE FROM users WHERE user_name = ?";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to delete user: " + e.getMessage());
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<User> loadAll() {
        ReentrantLock lock = DuckDBConnection.getLock();
        lock.lock();
        try {
            List<User> users = new ArrayList<>();
            String sql = "SELECT * FROM users";
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(fromResultSet(rs));
                }
            }
            return users;
        } catch (SQLException e) {
            System.err.println("Failed to load all users: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return new ArrayList<>();
    }

    private User fromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getObject("id") != null ? rs.getLong("id") : null;
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String userName = rs.getString("user_name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String tasksJson = rs.getString("tasks");
        String projectsJson = rs.getString("projects");

        List<Long> tasks = tasksJson != null ? GSON.fromJson(tasksJson, LONG_LIST_TYPE) : new ArrayList<>();
        List<Long> projects = projectsJson != null ? GSON.fromJson(projectsJson, LONG_LIST_TYPE) : new ArrayList<>();

        return new User(id, firstName, lastName, userName, email, passwordHash, tasks, projects);
    }
}
