package task.trak.app.server.dao.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.JedisPooled;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.backlog.BackLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisBacklogDAO implements EntityDAO<BackLog> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public void save(BackLog entity) {
        JedisPooled client = RedisConnection.getClient();
        client.set("trak:backlogs:" + entity.getName(), GSON.toJson(entity));
    }

    @Override
    public BackLog loadByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        String json = client.get("trak:backlogs:" + key);
        return json != null ? GSON.fromJson(json, BackLog.class) : null;
    }

    @Override
    public boolean deleteByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        return client.del("trak:backlogs:" + key) > 0;
    }

    @Override
    public List<BackLog> loadAll() {
        JedisPooled client = RedisConnection.getClient();
        Set<String> keys = client.keys("trak:backlogs:*");
        List<BackLog> result = new ArrayList<>();
        for (String k : keys) {
            String json = client.get(k);
            if (json != null) result.add(GSON.fromJson(json, BackLog.class));
        }
        return result;
    }
}
