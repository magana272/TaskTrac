package task.trak.app.server.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.backlog.BackLog;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonBacklogDAO implements EntityDAO<BackLog> {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public void save(BackLog b) {
        File file = new File(TTApp.storedir + File.separator + "backlog_" + b.getName() + ".json");
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(b, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BackLog loadByKey(String name) {
        File file = new File(TTApp.storedir + File.separator + "backlog_" + name + ".json");
        if (!file.exists()) return null;
        try (Reader reader = new FileReader(file)) {
            return GSON.fromJson(reader, BackLog.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteByKey(String name) {
        File file = new File(TTApp.storedir + File.separator + "backlog_" + name + ".json");
        return file.exists() && file.delete();
    }

    @Override
    public List<BackLog> loadAll() {
        List<BackLog> backlogs = new ArrayList<>();
        File dir = new File(TTApp.storedir);
        File[] files = dir.listFiles((d, name) -> name.startsWith("backlog_") && name.endsWith(".json"));
        if (files == null) return backlogs;
        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                BackLog b = GSON.fromJson(reader, BackLog.class);
                if (b != null) backlogs.add(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return backlogs;
    }
}
