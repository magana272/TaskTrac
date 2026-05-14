package task.trak.app.server.dao.duckdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.backlog.BackLog;
import task.trak.app.server.model.project.Project;
import task.trak.app.server.model.sprint.Sprint;
import task.trak.app.server.model.user.User;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DuckDBProjectDAO implements EntityDAO<Project> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    private static final Type USER_LIST_TYPE = new TypeToken<List<User>>() {}.getType();

    @Override
    public void save(Project entity) {
        String sql = "INSERT OR REPLACE INTO projects (id, project_name, summary, created_at, owner_json, members_json) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, entity.getId());
                ps.setString(2, entity.getName());
                ps.setString(3, entity.getSummary());
                ps.setObject(4, entity.getCreated_at() != null ? entity.getCreated_at().getTime() : null);
                ps.setString(5, entity.getOwner() != null ? GSON.toJson(entity.getOwner()) : null);
                ps.setString(6, entity.getMembers() != null ? GSON.toJson(entity.getMembers()) : null);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to save project: " + e.getMessage());
        }
    }

    @Override
    public Project loadByKey(String key) {
        String sql = "SELECT * FROM projects WHERE project_name = ?";
        try {
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
            System.err.println("Failed to load project: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean deleteByKey(String key) {
        String sql = "DELETE FROM projects WHERE project_name = ?";
        try {
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to delete project: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Project> loadAll() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects";
        try {
            Connection conn = DuckDBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projects.add(fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load all projects: " + e.getMessage());
        }
        return projects;
    }

    private Project fromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String projectName = rs.getString("project_name");
        String summary = rs.getString("summary");
        Long createdAtMs = rs.getObject("created_at") != null ? rs.getLong("created_at") : null;
        Date createdAt = createdAtMs != null ? new Date(createdAtMs) : null;
        String ownerJson = rs.getString("owner_json");
        String membersJson = rs.getString("members_json");

        User owner = ownerJson != null ? GSON.fromJson(ownerJson, User.class) : null;
        List<User> members = membersJson != null ? GSON.fromJson(membersJson, USER_LIST_TYPE) : new ArrayList<>();

        return new Project(id, createdAt, null, new ArrayList<>(), owner, members, projectName, summary);
    }
}
