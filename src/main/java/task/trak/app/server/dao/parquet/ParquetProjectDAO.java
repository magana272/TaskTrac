package task.trak.app.server.dao.parquet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.backlog.BackLog;
import task.trak.app.server.model.project.Project;
import task.trak.app.server.model.project.TrakProjectBuilder;
import task.trak.app.server.model.sprint.Sprint;
import task.trak.app.server.model.user.User;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ParquetProjectDAO implements EntityDAO<Project> {

    private static final String FILE_NAME = "Project.parquet";
    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    private static final String SCHEMA_JSON = """
            {
              "type": "record",
              "name": "Project",
              "fields": [
                {"name": "id", "type": ["null", "long"], "default": null},
                {"name": "project_name", "type": ["null", "string"], "default": null},
                {"name": "summary", "type": ["null", "string"], "default": null},
                {"name": "created_at", "type": ["null", "long"], "default": null},
                {"name": "owner_json", "type": ["null", "string"], "default": null},
                {"name": "members_json", "type": ["null", "string"], "default": null},
                {"name": "backlog_json", "type": ["null", "string"], "default": null},
                {"name": "sprints_json", "type": ["null", "string"], "default": null}
              ]
            }
            """;

    private static final Schema SCHEMA = new Schema.Parser().parse(SCHEMA_JSON);

    @Override
    public void save(Project entity) {
        List<Project> all = loadAll();
        all.removeIf(p -> entity.getName() != null && entity.getName().equals(p.getName()));
        all.add(entity);
        writeAll(all);
    }

    @Override
    public Project loadByKey(String name) {
        return loadAll().stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean deleteByKey(String name) {
        List<Project> all = loadAll();
        int before = all.size();
        all.removeIf(p -> name.equals(p.getName()));
        if (all.size() == before) return false;
        writeAll(all);
        return true;
    }

    @Override
    public List<Project> loadAll() {
        List<GenericRecord> records = ParquetHelper.readAll(FILE_NAME, SCHEMA);
        List<Project> projects = new ArrayList<>();
        for (GenericRecord r : records) {
            projects.add(fromRecord(r));
        }
        return projects;
    }

    private void writeAll(List<Project> projects) {
        List<GenericRecord> records = projects.stream().map(this::toRecord).collect(Collectors.toList());
        ParquetHelper.writeAll(FILE_NAME, SCHEMA, records);
    }

    private GenericRecord toRecord(Project p) {
        GenericRecord record = new GenericData.Record(SCHEMA);
        record.put("id", p.getId());
        record.put("project_name", p.getName());
        record.put("summary", p.getSummary());
        record.put("created_at", p.getCreated_at() != null ? p.getCreated_at().getTime() : null);
        record.put("owner_json", p.getOwner() != null ? GSON.toJson(p.getOwner()) : null);
        record.put("members_json", p.getMembers() != null ? GSON.toJson(p.getMembers()) : null);
        record.put("backlog_json", p.getBack_log() != null ? GSON.toJson(p.getBack_log()) : null);
        record.put("sprints_json", p.getSprints() != null ? GSON.toJson(p.getSprints()) : null);
        return record;
    }

    private Project fromRecord(GenericRecord r) {
        String ownerJson = r.get("owner_json") != null ? r.get("owner_json").toString() : null;
        String membersJson = r.get("members_json") != null ? r.get("members_json").toString() : null;
        String backlogJson = r.get("backlog_json") != null ? r.get("backlog_json").toString() : null;
        String sprintsJson = r.get("sprints_json") != null ? r.get("sprints_json").toString() : null;

        User owner = ownerJson != null ? GSON.fromJson(ownerJson, User.class) : null;
        List<User> members = membersJson != null
                ? GSON.fromJson(membersJson, new TypeToken<List<User>>() {
        }.getType())
                : null;
        BackLog backlog = backlogJson != null ? GSON.fromJson(backlogJson, BackLog.class) : null;
        List<Sprint> sprints = sprintsJson != null
                ? GSON.fromJson(sprintsJson, new TypeToken<List<Sprint>>() {
        }.getType())
                : null;

        String name = r.get("project_name") != null ? r.get("project_name").toString() : null;
        String summary = r.get("summary") != null ? r.get("summary").toString() : null;

        Long id = r.get("id") != null ? (Long) r.get("id") : null;
        Long createdAtMs = r.get("created_at") != null ? (Long) r.get("created_at") : null;

        TrakProjectBuilder builder = new TrakProjectBuilder();
        if (id != null) builder.setID(id);
        if (name != null) builder.setProjectName(name);
        if (summary != null) builder.setSummary(summary);
        if (owner != null) builder.setOwner(owner);
        if (members != null) builder.setMembers(members);
        if (backlog != null) builder.setBack_log(backlog);
        if (sprints != null) builder.setSprints(sprints);
        if (createdAtMs != null) builder.setCreationDate(new Date(createdAtMs));
        return builder.build();
    }
}
