package task.trak.app.server.dao.parquet;

import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.user.User;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParquetUserDAO implements EntityDAO<User> {

    private static final String FILE_NAME = "User.parquet";

    private static final String SCHEMA_JSON = """
            {
              "type": "record",
              "name": "User",
              "fields": [
                {"name": "id", "type": ["null", "long"], "default": null},
                {"name": "first_name", "type": ["null", "string"], "default": null},
                {"name": "last_name", "type": ["null", "string"], "default": null},
                {"name": "user_name", "type": ["null", "string"], "default": null},
                {"name": "email", "type": ["null", "string"], "default": null},
                {"name": "password_hash", "type": ["null", "string"], "default": null},
                {"name": "tasks", "type": {"type": "array", "items": "long"}, "default": []},
                {"name": "projects", "type": {"type": "array", "items": "long"}, "default": []}
              ]
            }
            """;

    private static final Schema SCHEMA = new Schema.Parser().parse(SCHEMA_JSON);

    @Override
    public void save(User entity) {
        List<User> all = loadAll();
        all.removeIf(u -> entity.getUser_name() != null && entity.getUser_name().equals(u.getUser_name()));
        all.add(entity);
        writeAll(all);
    }

    @Override
    public User loadByKey(String username) {
        return loadAll().stream()
                .filter(u -> username.equals(u.getUser_name()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean deleteByKey(String username) {
        List<User> all = loadAll();
        int before = all.size();
        all.removeIf(u -> username.equals(u.getUser_name()));
        if (all.size() == before) return false;
        writeAll(all);
        return true;
    }

    @Override
    public List<User> loadAll() {
        List<GenericRecord> records = ParquetHelper.readAll(FILE_NAME, SCHEMA);
        List<User> users = new ArrayList<>();
        for (GenericRecord r : records) {
            users.add(fromRecord(r));
        }
        return users;
    }

    private void writeAll(List<User> users) {
        List<GenericRecord> records = users.stream().map(this::toRecord).collect(Collectors.toList());
        ParquetHelper.writeAll(FILE_NAME, SCHEMA, records);
    }

    private GenericRecord toRecord(User u) {
        GenericRecord record = new GenericData.Record(SCHEMA);
        record.put("id", u.getID());
        record.put("first_name", u.getFirst_name());
        record.put("last_name", u.getLast_name());
        record.put("user_name", u.getUser_name());
        record.put("email", u.getEmail());
        record.put("password_hash", u.getPassword_hash());
        record.put("tasks", u.getTasks() != null ? u.getTasks() : new ArrayList<>());
        record.put("projects", u.getProjects() != null ? u.getProjects() : new ArrayList<>());
        return record;
    }

    private User fromRecord(GenericRecord r) {
        Long id = (Long) r.get("id");
        String firstName = r.get("first_name") != null ? r.get("first_name").toString() : null;
        String lastName = r.get("last_name") != null ? r.get("last_name").toString() : null;
        String userName = r.get("user_name") != null ? r.get("user_name").toString() : null;
        String email = r.get("email") != null ? r.get("email").toString() : null;
        String passwordHash = safeGet(r, "password_hash");
        List<Long> tasks = (List<Long>) r.get("tasks");
        List<Long> projects = (List<Long>) r.get("projects");
        return new User(id, firstName, lastName, userName, email, passwordHash,
                tasks != null ? new ArrayList<>(tasks) : new ArrayList<>(),
                projects != null ? new ArrayList<>(projects) : new ArrayList<>());
    }

    private String safeGet(GenericRecord r, String field) {
        try {
            Object val = r.get(field);
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
