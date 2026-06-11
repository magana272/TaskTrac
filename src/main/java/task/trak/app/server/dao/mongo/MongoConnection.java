package task.trak.app.server.dao.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import task.trak.app.client.config.WorkspaceConfig;

public class MongoConnection {

    private static MongoClient client;
    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            String uri = System.getenv("MONGO_URI");
            if (uri == null || uri.isBlank()) {
                WorkspaceConfig config = WorkspaceConfig.load();
                uri = config.getMongo_uri();
            }
            if (uri == null || uri.isBlank()) {
                throw new RuntimeException("MongoDB URI not configured. Set MONGO_URI or configure in workspace settings.");
            }
            String dbName = System.getenv("MONGO_DB");
            if (dbName == null || dbName.isBlank()) {
                WorkspaceConfig config = WorkspaceConfig.load();
                dbName = config.getMongo_db();
            }
            if (dbName == null || dbName.isBlank()) dbName = "trak";
            client = MongoClients.create(uri);
            database = client.getDatabase(dbName);
        }
        return database;
    }

    public static void close() {
        if (client != null) client.close();
    }
}
