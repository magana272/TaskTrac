package task.trak.app.server.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.backlog.BackLog;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoBacklogDAO implements EntityDAO<BackLog> {

    private MongoCollection<Document> collection() {
        return MongoConnection.getDatabase().getCollection("backlogs");
    }

    @Override
    public void save(BackLog b) {
        Document doc = toDocument(b);
        collection().replaceOne(
                Filters.eq("_id", b.getName()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public BackLog loadByKey(String key) {
        Document doc = collection().find(Filters.eq("_id", key)).first();
        return doc != null ? fromDocument(doc) : null;
    }

    @Override
    public boolean deleteByKey(String key) {
        return collection().deleteOne(Filters.eq("_id", key)).getDeletedCount() > 0;
    }

    @Override
    public List<BackLog> loadAll() {
        List<BackLog> backlogs = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection().find().iterator()) {
            while (cursor.hasNext()) {
                backlogs.add(fromDocument(cursor.next()));
            }
        }
        return backlogs;
    }

    private Document toDocument(BackLog b) {
        Document doc = new Document();
        doc.put("_id", b.getName());
        doc.put("id", b.getId());
        doc.put("name", b.getName());
        doc.put("project_name", b.getProject_name());
        doc.put("task_ids", b.getTask_ids());
        doc.put("created_at", b.getCreated_at());
        return doc;
    }

    private BackLog fromDocument(Document doc) {
        Long id = doc.getLong("id");
        String name = doc.getString("name");
        String projectName = doc.getString("project_name");
        List<Long> taskIds = doc.getList("task_ids", Long.class);
        Date createdAt = doc.getDate("created_at");
        return new BackLog(id, name, projectName, taskIds, createdAt);
    }
}
