package task.trak.app.server.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.user.User;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoUserDAO implements EntityDAO<User> {

    private MongoCollection<Document> collection() {
        return MongoConnection.getDatabase().getCollection("users");
    }

    @Override
    public void save(User u) {
        Document doc = toDocument(u);
        collection().replaceOne(
                Filters.eq("_id", u.getUser_name()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public User loadByKey(String key) {
        Document doc = collection().find(Filters.eq("_id", key)).first();
        return doc != null ? fromDocument(doc) : null;
    }

    @Override
    public boolean deleteByKey(String key) {
        return collection().deleteOne(Filters.eq("_id", key)).getDeletedCount() > 0;
    }

    @Override
    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection().find().iterator()) {
            while (cursor.hasNext()) {
                users.add(fromDocument(cursor.next()));
            }
        }
        return users;
    }

    private Document toDocument(User u) {
        Document doc = new Document();
        doc.put("_id", u.getUser_name());
        doc.put("id", u.getID());
        doc.put("first_name", u.getFirst_name());
        doc.put("last_name", u.getLast_name());
        doc.put("user_name", u.getUser_name());
        doc.put("email", u.getEmail());
        doc.put("password_hash", u.getPassword_hash());
        doc.put("tasks", u.getTasks());
        doc.put("projects", u.getProjects());
        return doc;
    }

    private User fromDocument(Document doc) {
        Long id = doc.getLong("id");
        String firstName = doc.getString("first_name");
        String lastName = doc.getString("last_name");
        String userName = doc.getString("user_name");
        String email = doc.getString("email");
        String passwordHash = doc.getString("password_hash");
        List<Long> tasks = doc.getList("tasks", Long.class);
        List<Long> projects = doc.getList("projects", Long.class);
        return new User(id, firstName, lastName, userName, email, passwordHash, tasks, projects);
    }
}
