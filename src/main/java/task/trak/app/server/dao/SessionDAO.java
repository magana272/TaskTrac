package task.trak.app.server.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.trak.model.Session;
import task.trak.app.client.cli.TTApp;

import java.io.*;

public class SessionDAO {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save(Session session) {
        File file = new File(TTApp.storedir + File.separator + "session.json");
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(session, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Session load() {
        File file = new File(TTApp.storedir + File.separator + "session.json");
        if (!file.exists()) return null;
        try (Reader reader = new FileReader(file)) {
            return GSON.fromJson(reader, Session.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clear() {
        File file = new File(TTApp.storedir + File.separator + "session.json");
        if (file.exists()) {
            file.delete();
        }
    }
}
