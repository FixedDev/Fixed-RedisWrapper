package me.fixeddev.fixedredis;

import com.google.gson.Gson;
import me.fixeddev.fixedredis.messenger.Messenger;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.Closeable;

public interface Redis extends Closeable {
    Messenger messenger();

    JedisPool rawConnection();

    Jedis listenerConnection();

    static RedisBuilder builder(Plugin plugin) {
        return new SimpleRedis.SimpleRedisBuilder(plugin);
    }

    interface RedisBuilder {
        RedisBuilder serverId(String id);

        RedisBuilder gson(Gson gson);

        RedisBuilder jedis(JedisPool connection, Jedis listenerConnection);

        RedisBuilder jedis(JedisBuilder builder);

        Redis build();
    }
}
