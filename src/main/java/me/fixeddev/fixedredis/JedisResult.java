package me.fixeddev.fixedredis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public interface JedisResult {
    Jedis listenerConnection();

    JedisPool pool();
}
