package task.trak.app.server.dao.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.JedisPooled;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisUserDAO implements EntityDAO<User> {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Override
    public void save(User entity) {
        JedisPooled client = RedisConnection.getClient();
        client.set("trak:users:" + entity.getUser_name(), GSON.toJson(entity));
    }

    @Override
    public User loadByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        String json = client.get("trak:users:" + key);
        return json != null ? GSON.fromJson(json, User.class) : null;
    }

    @Override
    public boolean deleteByKey(String key) {
        JedisPooled client = RedisConnection.getClient();
        return client.del("trak:users:" + key) > 0;
    }

    @Override
    public List<User> loadAll() {
        JedisPooled client = RedisConnection.getClient();
        Set<String> keys = client.keys("trak:users:*");
        List<User> result = new ArrayList<>();
        for (String k : keys) {
            String json = client.get(k);
            if (json != null) result.add(GSON.fromJson(json, User.class));
        }
        return result;
    }
}
