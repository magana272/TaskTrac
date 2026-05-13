package task.trak.app.server.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.project.Project;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonProjectDAO implements EntityDAO<Project> {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public void save(Project p) {
        File file = new File(TTApp.storedir + File.separator + p.getName() + ".json");
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(p, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Project loadByKey(String name) {
        File file = new File(TTApp.storedir + File.separator + name + ".json");
        if (!file.exists()) return null;
        try (Reader reader = new FileReader(file)) {
            return GSON.fromJson(reader, Project.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteByKey(String name) {
        File file = new File(TTApp.storedir + File.separator + name + ".json");
        return file.exists() && file.delete();
    }

    @Override
    public List<Project> loadAll() {
        List<Project> projects = new ArrayList<>();
        File dir = new File(TTApp.storedir);
        File[] files = dir.listFiles((d, name) ->
                name.endsWith(".json")
                        && !name.startsWith("user_")
                        && !name.startsWith("task_")
                        && !name.startsWith("sprint_")
                        && !name.startsWith("backlog_")
                        && !name.equals("session.json")
                        && !name.equals("workspace.json")
        );
        if (files == null) return projects;
        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                Project p = GSON.fromJson(reader, Project.class);
                if (p != null) projects.add(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return projects;
    }
}
