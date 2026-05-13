package task.trak.app.server.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.task.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonTaskDAO implements EntityDAO<Task> {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public void save(Task t) {
        File file = new File(TTApp.storedir + File.separator + "task_" + t.getId() + ".json");
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(t, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Task loadByKey(String id) {
        File file = new File(TTApp.storedir + File.separator + "task_" + id + ".json");
        if (!file.exists()) return null;
        try (Reader reader = new FileReader(file)) {
            return GSON.fromJson(reader, Task.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteByKey(String id) {
        File file = new File(TTApp.storedir + File.separator + "task_" + id + ".json");
        return file.exists() && file.delete();
    }

    @Override
    public List<Task> loadAll() {
        List<Task> tasks = new ArrayList<>();
        File dir = new File(TTApp.storedir);
        File[] files = dir.listFiles((d, name) -> name.startsWith("task_") && name.endsWith(".json"));
        if (files == null) return tasks;
        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                Task t = GSON.fromJson(reader, Task.class);
                if (t != null) tasks.add(t);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tasks;
    }
}
