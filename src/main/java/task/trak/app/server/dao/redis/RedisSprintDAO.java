package task.trak.app.server.dao.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.JedisPooled;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.sprint.Sprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisSprintDAO implements EntityDAO<Sprint> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public void save(Sprint entity) {
        JedisPooled client = RedisConnection.getClient();
        client.set("trak:sprints:" + String.valueOf(entity.getId()), GSON.toJson(entity));
    }

    @Override
    public Sprint loadByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        // Try numeric ID first
        try {
            Long.parseLong(key);
            String json = client.get("trak:sprints:" + key);
            if (json != null) return GSON.fromJson(json, Sprint.class);
        } catch (NumberFormatException ignored) {}
        // Fall back to scanning by name
        Set<String> keys = client.keys("trak:sprints:*");
        for (String k : keys) {
            String json = client.get(k);
            if (json != null) {
                Sprint sprint = GSON.fromJson(json, Sprint.class);
                if (key.equals(sprint.getName())) return sprint;
            }
        }
        return null;
    }

    @Override
    public boolean deleteByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        // Try numeric ID first
        try {
            Long.parseLong(key);
            if (client.del("trak:sprints:" + key) > 0) return true;
        } catch (NumberFormatException ignored) {}
        // Fall back to scanning by name
        Set<String> keys = client.keys("trak:sprints:*");
        for (String k : keys) {
            String json = client.get(k);
            if (json != null) {
                Sprint sprint = GSON.fromJson(json, Sprint.class);
                if (key.equals(sprint.getName())) {
                    return client.del(k) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public List<Sprint> loadAll() {
        JedisPooled client = RedisConnection.getClient();
        Set<String> keys = client.keys("trak:sprints:*");
        List<Sprint> result = new ArrayList<>();
        for (String k : keys) {
            String json = client.get(k);
            if (json != null) result.add(GSON.fromJson(json, Sprint.class));
        }
        return result;
    }
}
