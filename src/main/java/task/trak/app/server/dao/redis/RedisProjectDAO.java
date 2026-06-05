package task.trak.app.server.dao.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.JedisPooled;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.project.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisProjectDAO implements EntityDAO<Project> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public void save(Project entity) {
        JedisPooled client = RedisConnection.getClient();
        client.set("trak:projects:" + entity.getName(), GSON.toJson(entity));
    }

    @Override
    public Project loadByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        String json = client.get("trak:projects:" + key);
        return json != null ? GSON.fromJson(json, Project.class) : null;
    }

    @Override
    public boolean deleteByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        return client.del("trak:projects:" + key) > 0;
    }

    @Override
    public List<Project> loadAll() {
        JedisPooled client = RedisConnection.getClient();
        Set<String> keys = client.keys("trak:projects:*");
        List<Project> result = new ArrayList<>();
        for (String k : keys) {
            String json = client.get(k);
            if (json != null) result.add(GSON.fromJson(json, Project.class));
        }
        return result;
    }
}
