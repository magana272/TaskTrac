package task.trak.app.server.dao.redis;

import redis.clients.jedis.JedisPooled;

public class RedisConnection {

    private static JedisPooled client;

    public static JedisPooled getClient() {
        if (client == null) {
            String url = System.getenv("REDIS_URL");
            if (url == null) url = "redis://localhost:6379";
            client = new JedisPooled(url);
        }
        return client;
    }

    public static void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}
