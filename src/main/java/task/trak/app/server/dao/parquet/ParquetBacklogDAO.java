package task.trak.app.server.dao.parquet;

import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.backlog.BackLog;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ParquetBacklogDAO implements EntityDAO<BackLog> {

    private static final String FILE_NAME = "Backlog.parquet";

    private static final String SCHEMA_JSON = """
            {
              "type": "record",
              "name": "Backlog",
              "fields": [
                {"name": "id", "type": ["null", "long"], "default": null},
                {"name": "name", "type": ["null", "string"], "default": null},
                {"name": "project_name", "type": ["null", "string"], "default": null},
                {"name": "task_ids", "type": {"type": "array", "items": "long"}, "default": []},
                {"name": "created_at", "type": ["null", "long"], "default": null}
              ]
            }
            """;

    private static final Schema SCHEMA = new Schema.Parser().parse(SCHEMA_JSON);

    @Override
    public void save(BackLog entity) {
        List<BackLog> all = loadAll();
        all.removeIf(b -> entity.getName() != null && entity.getName().equals(b.getName()));
        all.add(entity);
        writeAll(all);
    }

    @Override
    public BackLog loadByKey(String name) {
        return loadAll().stream()
                .filter(b -> name.equals(b.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean deleteByKey(String name) {
        List<BackLog> all = loadAll();
        int before = all.size();
        all.removeIf(b -> name.equals(b.getName()));
        if (all.size() == before) return false;
        writeAll(all);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BackLog> loadAll() {
        List<GenericRecord> records = ParquetHelper.readAll(FILE_NAME, SCHEMA);
        List<BackLog> backlogs = new ArrayList<>();
        for (GenericRecord r : records) {
            Long id = (Long) r.get("id");
            String name = r.get("name") != null ? r.get("name").toString() : null;
            String projectName = r.get("project_name") != null ? r.get("project_name").toString() : null;
            List<Long> taskIds = (List<Long>) r.get("task_ids");
            Long createdAtMs = (Long) r.get("created_at");
            backlogs.add(new BackLog(id, name, projectName,
                    taskIds != null ? new ArrayList<>(taskIds) : new ArrayList<>(),
                    createdAtMs != null ? new Date(createdAtMs) : null));
        }
        return backlogs;
    }

    private void writeAll(List<BackLog> backlogs) {
        List<GenericRecord> records = backlogs.stream().map(this::toRecord).collect(Collectors.toList());
        ParquetHelper.writeAll(FILE_NAME, SCHEMA, records);
    }

    private GenericRecord toRecord(BackLog b) {
        GenericRecord record = new GenericData.Record(SCHEMA);
        record.put("id", b.getId());
        record.put("name", b.getName());
        record.put("project_name", b.getProject_name());
        record.put("task_ids", b.getTask_ids() != null ? b.getTask_ids() : new ArrayList<>());
        record.put("created_at", b.getCreated_at() != null ? b.getCreated_at().getTime() : null);
        return record;
    }
}
