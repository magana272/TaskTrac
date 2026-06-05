package task.trak.app.server.dao.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.JedisPooled;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisTaskDAO implements EntityDAO<Task> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public void save(Task entity) {
        JedisPooled client = RedisConnection.getClient();
        client.set("trak:tasks:" + entity.getId(), GSON.toJson(entity));
    }

    @Override
    public Task loadByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        String json = client.get("trak:tasks:" + key);
        return json != null ? GSON.fromJson(json, Task.class) : null;
    }

    @Override
    public boolean deleteByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        return client.del("trak:tasks:" + key) > 0;
    }

    @Override
    public List<Task> loadAll() {
        JedisPooled client = RedisConnection.getClient();
        Set<String> keys = client.keys("trak:tasks:*");
        List<Task> result = new ArrayList<>();
        for (String k : keys) {
            String json = client.get(k);
            if (json != null) result.add(GSON.fromJson(json, Task.class));
        }
        return result;
    }
}
