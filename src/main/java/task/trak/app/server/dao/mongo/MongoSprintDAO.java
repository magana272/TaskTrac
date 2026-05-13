package task.trak.app.server.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.sprint.Sprint;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoSprintDAO implements EntityDAO<Sprint> {

    private MongoCollection<Document> collection() {
        return MongoConnection.getDatabase().getCollection("sprints");
    }

    @Override
    public void save(Sprint s) {
        Document doc = toDocument(s);
        collection().replaceOne(
                Filters.eq("_id", s.getId()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public Sprint loadByKey(String key) {
        // Try parsing as Long (ID lookup) first
        try {
            Long id = Long.parseLong(key);
            Document doc = collection().find(Filters.eq("_id", id)).first();
            if (doc != null) return fromDocument(doc);
        } catch (NumberFormatException ignored) {
            // Fall through to name search
        }
        // Fallback to name search
        Document doc = collection().find(Filters.eq("name", key)).first();
        return doc != null ? fromDocument(doc) : null;
    }

    @Override
    public boolean deleteByKey(String key) {
        // Try as ID first
        try {
            Long id = Long.parseLong(key);
            if (collection().deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0) return true;
        } catch (NumberFormatException ignored) {
            // Fall through to name search
        }
        // Fallback to name deletion
        return collection().deleteOne(Filters.eq("name", key)).getDeletedCount() > 0;
    }

    @Override
    public List<Sprint> loadAll() {
        List<Sprint> sprints = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection().find().iterator()) {
            while (cursor.hasNext()) {
                sprints.add(fromDocument(cursor.next()));
            }
        }
        return sprints;
    }

    private Document toDocument(Sprint s) {
        Document doc = new Document();
        doc.put("_id", s.getId());
        doc.put("id", s.getId());
        doc.put("project_name", s.getProject_name());
        doc.put("name", s.getName());
        doc.put("task_ids", s.getTask_ids());
        doc.put("start_date", s.getStart_date());
        doc.put("end_date", s.getEnd_date());
        return doc;
    }

    private Sprint fromDocument(Document doc) {
        Long id = doc.getLong("id");
        String projectName = doc.getString("project_name");
        String name = doc.getString("name");
        List<Long> taskIds = doc.getList("task_ids", Long.class);
        Date startDate = doc.getDate("start_date");
        Date endDate = doc.getDate("end_date");
        return new Sprint(id, projectName, name, taskIds, startDate, endDate);
    }
}
