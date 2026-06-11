package task.trak.app.server.dao.redis;

import redis.clients.jedis.JedisPooled;
import task.trak.app.client.config.WorkspaceConfig;

public class RedisConnection {

    private static JedisPooled client;

    public static JedisPooled getClient() {
        if (client == null) {
            String url = System.getenv("REDIS_URL");
            if (url == null || url.isBlank()) {
                WorkspaceConfig config = WorkspaceConfig.load();
                url = config.getRedis_url();
            }
            if (url == null || url.isBlank()) url = "redis://localhost:6379";
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
