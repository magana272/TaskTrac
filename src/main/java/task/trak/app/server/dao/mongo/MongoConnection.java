package task.trak.app.server.dao.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

    private static MongoClient client;
    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            String uri = System.getenv("MONGO_URI");
            if (uri == null) throw new RuntimeException("MONGO_URI environment variable not set");
            String dbName = System.getenv("MONGO_DB");
            if (dbName == null) dbName = "trak";
            client = MongoClients.create(uri);
            database = client.getDatabase(dbName);
        }
        return database;
    }

    public static void close() {
        if (client != null) client.close();
    }
}
