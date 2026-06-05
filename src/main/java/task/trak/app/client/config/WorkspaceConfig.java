package task.trak.app.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.trak.app.client.cli.TTApp;

import java.io.*;

public class WorkspaceConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String store_format;

    public WorkspaceConfig() {
        this.store_format = "duckdb";
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

    public void save() {
        File file = new File(TTApp.storedir + File.separator + "workspace.json");
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
