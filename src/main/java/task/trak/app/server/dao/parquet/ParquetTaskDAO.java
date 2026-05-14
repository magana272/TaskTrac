package task.trak.app.server.dao.parquet;

import task.trak.api.service.STATE;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.task.Task;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ParquetTaskDAO implements EntityDAO<Task> {

    private static final String FILE_NAME = "Task.parquet";

    private static final String SCHEMA_JSON = """
            {
              "type": "record",
              "name": "Task",
              "fields": [
                {"name": "id", "type": ["null", "long"], "default": null},
                {"name": "project_name", "type": ["null", "string"], "default": null},
                {"name": "assigned_to", "type": ["null", "string"], "default": null},
                {"name": "title", "type": ["null", "string"], "default": null},
                {"name": "status", "type": ["null", "string"], "default": null},
                {"name": "created_at", "type": ["null", "long"], "default": null},
                {"name": "completed_at", "type": ["null", "long"], "default": null},
                {"name": "summary", "type": ["null", "string"], "default": null},
                {"name": "deadline", "type": ["null", "long"], "default": null},
                {"name": "estimate", "type": ["null", "string"], "default": null},
                {"name": "time_started", "type": ["null", "long"], "default": null},
                {"name": "time_spent_ms", "type": ["null", "long"], "default": null},
                {"name": "completion_note", "type": ["null", "string"], "default": null}
              ]
            }
            """;

    private static final Schema SCHEMA = new Schema.Parser().parse(SCHEMA_JSON);

    @Override
    public void save(Task entity) {
        List<Task> all = loadAll();
        all.removeIf(t -> entity.getId() != null && entity.getId().equals(t.getId()));
        all.add(entity);
        writeAll(all);
    }

    @Override
    public Task loadByKey(String id) {
        Long taskId = Long.parseLong(id);
        return loadAll().stream()
                .filter(t -> taskId.equals(t.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean deleteByKey(String id) {
        Long taskId = Long.parseLong(id);
        List<Task> all = loadAll();
        int before = all.size();
        all.removeIf(t -> taskId.equals(t.getId()));
        if (all.size() == before) return false;
        writeAll(all);
        return true;
    }

    @Override
    public List<Task> loadAll() {
        List<GenericRecord> records = ParquetHelper.readAll(FILE_NAME, SCHEMA);
        List<Task> tasks = new ArrayList<>();
        for (GenericRecord r : records) {
            tasks.add(fromRecord(r));
        }
        return tasks;
    }

    private void writeAll(List<Task> tasks) {
        List<GenericRecord> records = tasks.stream().map(this::toRecord).collect(Collectors.toList());
        ParquetHelper.writeAll(FILE_NAME, SCHEMA, records);
    }

    private GenericRecord toRecord(Task t) {
        GenericRecord record = new GenericData.Record(SCHEMA);
        record.put("id", t.getId());
        record.put("project_name", t.getProject_name());
        record.put("assigned_to", t.getAssigned_to());
        record.put("title", t.getTitle());
        record.put("status", t.getStatus() != null ? t.getStatus().name() : null);
        record.put("created_at", t.getCreated_at() != null ? t.getCreated_at().getTime() : null);
        record.put("completed_at", t.getCompleted_at() != null ? t.getCompleted_at().getTime() : null);
        record.put("summary", t.getSummary());
        record.put("deadline", t.getDeadline() != null ? t.getDeadline().getTime() : null);
        record.put("estimate", t.getEstimate());
        record.put("time_started", t.getTime_started());
        record.put("time_spent_ms", t.getTime_spent_ms());
        record.put("completion_note", t.getCompletion_note());
        return record;
    }

    private Task fromRecord(GenericRecord r) {
        Long id = (Long) r.get("id");
        String projectName = r.get("project_name") != null ? r.get("project_name").toString() : null;
        String assignedTo = r.get("assigned_to") != null ? r.get("assigned_to").toString() : null;
        String title = r.get("title") != null ? r.get("title").toString() : null;
        String statusStr = r.get("status") != null ? r.get("status").toString() : null;
        STATE status = statusStr != null ? STATE.valueOf(statusStr) : STATE.READY;
        Long createdAtMs = (Long) r.get("created_at");
        Long completedAtMs = (Long) r.get("completed_at");
        Date createdAt = createdAtMs != null ? new Date(createdAtMs) : null;
        Date completedAt = completedAtMs != null ? new Date(completedAtMs) : null;
        String summary = r.get("summary") != null ? r.get("summary").toString() : null;

        Task task = new Task(id, projectName, assignedTo, title, status, createdAt, completedAt, summary);
        Long deadlineMs = safeGetLong(r, "deadline");
        if (deadlineMs != null) task.setDeadline(new Date(deadlineMs));
        String estimate = safeGetString(r, "estimate");
        if (estimate != null) task.setEstimate(estimate);
        task.setTime_started(safeGetLong(r, "time_started"));
        task.setTime_spent_ms(safeGetLong(r, "time_spent_ms"));
        task.setCompletion_note(safeGetString(r, "completion_note"));
        return task;
    }

    private Long safeGetLong(GenericRecord r, String field) {
        try {
            return (Long) r.get(field);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeGetString(GenericRecord r, String field) {
        try {
            Object val = r.get(field);
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
