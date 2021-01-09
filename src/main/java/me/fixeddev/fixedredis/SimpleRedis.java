package me.fixeddev.fixedredis;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import me.fixeddev.fixedredis.messenger.Messenger;
import me.fixeddev.fixedredis.messenger.RedisMessenger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.UUID;

public class SimpleRedis implements Redis {

    private String serverId;

    private Gson gson;
    private JedisPool jedisPool;
    private Jedis listenerConnection;

    private Messenger messenger;

    SimpleRedis(String serverId, Gson gson, JedisPool jedisPool, Jedis listenerConnection) {
        this.serverId = serverId;
        this.gson = gson;
        this.jedisPool = jedisPool;
        this.listenerConnection = listenerConnection;

        this.messenger = new RedisMessenger(jedisPool, listenerConnection, serverId, gson);
    }

    @Override
    public Messenger messenger() {
        return messenger;
    }

    @Override
    public JedisPool rawConnection() {
        return jedisPool;
    }

    @Override
    public Jedis listenerConnection() {
        return listenerConnection;
    }

    @Override
    public void close() throws IOException {
        messenger.close();
        messenger = null;
        rawConnection().close();
        listenerConnection.close();
    }

    static class SimpleRedisBuilder implements Builder {
        private String serverId = UUID.randomUUID().toString();

        private Gson gson;
        private JedisPool jedisPool;
        private Jedis listenerConnection;

        @Override
        public Builder serverId(String id) {
            Preconditions.checkNotNull(id);
            this.serverId = id;

            return this;
        }

        @Override
        public Builder gson(Gson gson) {
            Preconditions.checkNotNull(gson);
            this.gson = gson;

            return this;
        }

        @Override
        public Builder jedis(JedisPool pool, Jedis listenerConnection) {
            Preconditions.checkNotNull(pool);
            Preconditions.checkNotNull(listenerConnection);

            this.jedisPool = pool;
            this.listenerConnection = listenerConnection;

            return this;
        }

        @Override
        public Builder jedis(JedisBuilder builder) {
            Preconditions.checkNotNull(builder);

            JedisResult result = builder.build();

            jedis(result.pool(), result.listenerConnection());
            return this;
        }

        @Override
        public Redis build() {
            if(gson == null){
                this.gson = new Gson();
            }

            if(jedisPool == null){
                jedisPool = new JedisPool();
                listenerConnection = new Jedis();
            }

            return new SimpleRedis(serverId, gson, jedisPool, listenerConnection);
        }
    }
}
