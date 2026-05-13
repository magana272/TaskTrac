package task.trak.app.server.dao.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.user.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonUserDAO implements EntityDAO<User> {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Override
    public void save(User u) {
        File file = new File(TTApp.storedir + File.separator + "user_" + u.getUser_name() + ".json");
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(u, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User loadByKey(String username) {
        File file = new File(TTApp.storedir + File.separator + "user_" + username + ".json");
        if (!file.exists()) return null;
        try (Reader reader = new FileReader(file)) {
            return GSON.fromJson(reader, User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteByKey(String username) {
        File file = new File(TTApp.storedir + File.separator + "user_" + username + ".json");
        return file.exists() && file.delete();
    }

    @Override
    public List<User> loadAll() {
        List<User> users = new ArrayList<>();
        File dir = new File(TTApp.storedir);
        File[] files = dir.listFiles((d, name) -> name.startsWith("user_") && name.endsWith(".json"));
        if (files == null) return users;
        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                User u = GSON.fromJson(reader, User.class);
                if (u != null) users.add(u);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return users;
    }
}
