package task.trak.app.server.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.sprint.Sprint;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonSprintDAO implements EntityDAO<Sprint> {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public void save(Sprint s) {
        File file = new File(TTApp.storedir + File.separator + "sprint_" + s.getId() + ".json");
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(s, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Sprint loadByKey(String key) {
        // Try as ID first
        File file = new File(TTApp.storedir + File.separator + "sprint_" + key + ".json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                return GSON.fromJson(reader, Sprint.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Fall back to searching by name
        return loadAll().stream()
                .filter(s -> key.equals(s.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean deleteByKey(String key) {
        // Try as ID
        File file = new File(TTApp.storedir + File.separator + "sprint_" + key + ".json");
        if (file.exists()) return file.delete();
        // Try by name — find the sprint and delete by its ID
        Sprint s = loadAll().stream().filter(sp -> key.equals(sp.getName())).findFirst().orElse(null);
        if (s != null) {
            File f = new File(TTApp.storedir + File.separator + "sprint_" + s.getId() + ".json");
            return f.exists() && f.delete();
        }
        return false;
    }

    @Override
    public List<Sprint> loadAll() {
        List<Sprint> sprints = new ArrayList<>();
        File dir = new File(TTApp.storedir);
        File[] files = dir.listFiles((d, name) -> name.startsWith("sprint_") && name.endsWith(".json"));
        if (files == null) return sprints;
        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                Sprint s = GSON.fromJson(reader, Sprint.class);
                if (s != null) sprints.add(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sprints;
    }
}
