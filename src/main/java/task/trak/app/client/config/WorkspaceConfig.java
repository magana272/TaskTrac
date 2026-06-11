package task.trak.app.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.trak.app.client.cli.TTApp;

import java.io.*;

public class WorkspaceConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String store_format;
    private String theme;
    private String mode;       // "local" or "remote"
    private String server_url; // remote server URL
    private String redis_url;
    private String mongo_uri;
    private String mongo_db;
    private String build_timestamp;

    public WorkspaceConfig() {
        this.store_format = "duckdb";
        this.theme = "dark";
        this.mode = "local";
    }

    public static WorkspaceConfig load() {
        File file = new File(TTApp.storedir + File.separator + "workspace.json");
        if (!file.exists()) return new WorkspaceConfig();
        try (Reader reader = new FileReader(file)) {
            WorkspaceConfig config = GSON.fromJson(reader, WorkspaceConfig.class);
            return config != null ? config : new WorkspaceConfig();
        } catch (IOException e) {
            return new WorkspaceConfig();
        }
    }

    public String getStore_format() {
        return store_format;
    }

    public void setStore_format(String store_format) {
        this.store_format = store_format;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getServer_url() { return server_url; }
    public void setServer_url(String server_url) { this.server_url = server_url; }

    public String getRedis_url() { return redis_url; }
    public void setRedis_url(String redis_url) { this.redis_url = redis_url; }

    public String getMongo_uri() { return mongo_uri; }
    public void setMongo_uri(String mongo_uri) { this.mongo_uri = mongo_uri; }

    public String getMongo_db() { return mongo_db; }
    public void setMongo_db(String mongo_db) { this.mongo_db = mongo_db; }

    public String getBuild_timestamp() { return build_timestamp; }
    public void setBuild_timestamp(String build_timestamp) { this.build_timestamp = build_timestamp; }

    public void save() {
        File file = new File(TTApp.storedir + File.separator + "workspace.json");
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
