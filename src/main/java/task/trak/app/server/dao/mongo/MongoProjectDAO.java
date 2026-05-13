package task.trak.app.server.dao.mongo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.backlog.BackLog;
import task.trak.app.server.model.project.Project;
import task.trak.app.server.model.project.TrakProjectBuilder;
import task.trak.app.server.model.sprint.Sprint;
import task.trak.app.server.model.user.User;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoProjectDAO implements EntityDAO<Project> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    private MongoCollection<Document> collection() {
        return MongoConnection.getDatabase().getCollection("projects");
    }

    @Override
    public void save(Project p) {
        Document doc = toDocument(p);
        collection().replaceOne(
                Filters.eq("_id", p.getName()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public Project loadByKey(String key) {
        Document doc = collection().find(Filters.eq("_id", key)).first();
        return doc != null ? fromDocument(doc) : null;
    }

    @Override
    public boolean deleteByKey(String key) {
        return collection().deleteOne(Filters.eq("_id", key)).getDeletedCount() > 0;
    }

    @Override
    public List<Project> loadAll() {
        List<Project> projects = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection().find().iterator()) {
            while (cursor.hasNext()) {
                projects.add(fromDocument(cursor.next()));
            }
        }
        return projects;
    }

    private Document toDocument(Project p) {
        Document doc = new Document();
        doc.put("_id", p.getName());
        doc.put("id", p.getId());
        doc.put("project_name", p.getName());
        doc.put("summary", p.getSummary());
        doc.put("created_at", p.getCreated_at());
        doc.put("owner_json", p.getOwner() != null ? GSON.toJson(p.getOwner()) : null);
        doc.put("members_json", p.getMembers() != null ? GSON.toJson(p.getMembers()) : null);
        doc.put("backlog_json", p.getBack_log() != null ? GSON.toJson(p.getBack_log()) : null);
        doc.put("sprints_json", p.getSprints() != null ? GSON.toJson(p.getSprints()) : null);
        return doc;
    }

    private Project fromDocument(Document doc) {
        String ownerJson = doc.getString("owner_json");
        String membersJson = doc.getString("members_json");
        String backlogJson = doc.getString("backlog_json");
        String sprintsJson = doc.getString("sprints_json");

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

        Long id = doc.getLong("id");
        Date createdAt = doc.getDate("created_at");
        String name = doc.getString("project_name");
        String summary = doc.getString("summary");

        TrakProjectBuilder builder = new TrakProjectBuilder();
        if (id != null) builder.setID(id);
        if (name != null) builder.setProjectName(name);
        if (summary != null) builder.setSummary(summary);
        if (owner != null) builder.setOwner(owner);
        if (members != null) builder.setMembers(members);
        if (backlog != null) builder.setBack_log(backlog);
        if (sprints != null) builder.setSprints(sprints);
        if (createdAt != null) builder.setCreationDate(createdAt);
        return builder.build();
    }
}
