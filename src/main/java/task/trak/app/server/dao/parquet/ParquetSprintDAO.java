package task.trak.app.server.dao.parquet;

import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.sprint.Sprint;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ParquetSprintDAO implements EntityDAO<Sprint> {

    private static final String FILE_NAME = "Sprint.parquet";

    private static final String SCHEMA_JSON = """
            {
              "type": "record",
              "name": "Sprint",
              "fields": [
                {"name": "id", "type": ["null", "long"], "default": null},
                {"name": "project_name", "type": ["null", "string"], "default": null},
                {"name": "name", "type": ["null", "string"], "default": null},
                {"name": "task_ids", "type": {"type": "array", "items": "long"}, "default": []},
                {"name": "start_date", "type": ["null", "long"], "default": null},
                {"name": "end_date", "type": ["null", "long"], "default": null}
              ]
            }
            """;

    private static final Schema SCHEMA = new Schema.Parser().parse(SCHEMA_JSON);

    @Override
    public void save(Sprint entity) {
        List<Sprint> all = loadAll();
        all.removeIf(s -> entity.getId() != null && entity.getId().equals(s.getId()));
        all.add(entity);
        writeAll(all);
    }

    @Override
    public Sprint loadByKey(String key) {
        return loadAll().stream()
                .filter(s -> key.equals(s.getName()) || key.equals(String.valueOf(s.getId())))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean deleteByKey(String key) {
        List<Sprint> all = loadAll();
        int before = all.size();
        all.removeIf(s -> key.equals(s.getName()) || key.equals(String.valueOf(s.getId())));
        if (all.size() == before) return false;
        writeAll(all);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Sprint> loadAll() {
        List<GenericRecord> records = ParquetHelper.readAll(FILE_NAME, SCHEMA);
        List<Sprint> sprints = new ArrayList<>();
        for (GenericRecord r : records) {
            Long id = (Long) r.get("id");
            String projectName = r.get("project_name") != null ? r.get("project_name").toString() : null;
            String name = r.get("name") != null ? r.get("name").toString() : null;
            List<Long> taskIds = (List<Long>) r.get("task_ids");
            Long startMs = (Long) r.get("start_date");
            Long endMs = (Long) r.get("end_date");
            sprints.add(new Sprint(id, projectName, name,
                    taskIds != null ? new ArrayList<>(taskIds) : new ArrayList<>(),
                    startMs != null ? new Date(startMs) : null,
                    endMs != null ? new Date(endMs) : null));
        }
        return sprints;
    }

    private void writeAll(List<Sprint> sprints) {
        List<GenericRecord> records = sprints.stream().map(this::toRecord).collect(Collectors.toList());
        ParquetHelper.writeAll(FILE_NAME, SCHEMA, records);
    }

    private GenericRecord toRecord(Sprint s) {
        GenericRecord record = new GenericData.Record(SCHEMA);
        record.put("id", s.getId());
        record.put("project_name", s.getProject_name());
        record.put("name", s.getName());
        record.put("task_ids", s.getTask_ids() != null ? s.getTask_ids() : new ArrayList<>());
        record.put("start_date", s.getStart_date() != null ? s.getStart_date().getTime() : null);
        record.put("end_date", s.getEnd_date() != null ? s.getEnd_date().getTime() : null);
        return record;
    }
}
