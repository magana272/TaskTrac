package task.trak.app.server.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import task.trak.api.service.STATE;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.task.Task;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoTaskDAO implements EntityDAO<Task> {

    private MongoCollection<Document> collection() {
        return MongoConnection.getDatabase().getCollection("tasks");
    }

    @Override
    public void save(Task t) {
        Document doc = toDocument(t);
        collection().replaceOne(
                Filters.eq("_id", t.getId()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public Task loadByKey(String key) {
        Long id = Long.parseLong(key);
        Document doc = collection().find(Filters.eq("_id", id)).first();
        return doc != null ? fromDocument(doc) : null;
    }

    @Override
    public boolean deleteByKey(String key) {
        Long id = Long.parseLong(key);
        return collection().deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
    }

    @Override
    public List<Task> loadAll() {
        List<Task> tasks = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection().find().iterator()) {
            while (cursor.hasNext()) {
                tasks.add(fromDocument(cursor.next()));
            }
        }
        return tasks;
    }

    private Document toDocument(Task t) {
        Document doc = new Document();
        doc.put("_id", t.getId());
        doc.put("id", t.getId());
        doc.put("project_name", t.getProject_name());
        doc.put("assigned_to", t.getAssigned_to());
        doc.put("title", t.getTitle());
        doc.put("status", t.getStatus() != null ? t.getStatus().name() : null);
        doc.put("created_at", t.getCreated_at());
        doc.put("completed_at", t.getCompleted_at());
        doc.put("summary", t.getSummary());
        doc.put("deadline", t.getDeadline());
        doc.put("estimate", t.getEstimate());
        doc.put("time_started", t.getTime_started());
        doc.put("time_spent_ms", t.getTime_spent_ms());
        return doc;
    }

    private Task fromDocument(Document doc) {
        Long id = doc.getLong("id");
        String projectName = doc.getString("project_name");
        String assignedTo = doc.getString("assigned_to");
        String title = doc.getString("title");
        String statusStr = doc.getString("status");
        STATE status = statusStr != null ? STATE.valueOf(statusStr) : null;
        Date createdAt = doc.getDate("created_at");
        Date completedAt = doc.getDate("completed_at");
        String summary = doc.getString("summary");

        Task task = new Task(id, projectName, assignedTo, title, status, createdAt, completedAt, summary);

        Date deadline = doc.getDate("deadline");
        if (deadline != null) task.setDeadline(deadline);

        String estimate = doc.getString("estimate");
        if (estimate != null) task.setEstimate(estimate);

        Long timeStarted = doc.getLong("time_started");
        if (timeStarted != null) task.setTime_started(timeStarted);

        Long timeSpentMs = doc.getLong("time_spent_ms");
        if (timeSpentMs != null) task.setTime_spent_ms(timeSpentMs);

        return task;
    }
}
