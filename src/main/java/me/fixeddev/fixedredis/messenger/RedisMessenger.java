package me.fixeddev.fixedredis.messenger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisMessenger implements Messenger {

    private String serverId;
    private Gson gson;

    private JedisPool messengerPool;
    private Jedis listenerConnection;

    private Map<String, RedisChannel<?>> channels;

    private JedisPubSub pubSub;
    private JsonParser parser = new JsonParser();

    public RedisMessenger(JedisPool jedisPool, Jedis listenerConnection, String serverId, Gson gson) {
        this.serverId = serverId;
        this.gson = gson;

        messengerPool = jedisPool;
        this.listenerConnection = listenerConnection;

        channels = new ConcurrentHashMap<>();

        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                RedisChannel<Object> channelObject = (RedisChannel<Object>) channels.get(channel);

                if (channelObject == null) {
                    return;
                }

                JsonObject jsonMessage = parser.parse(message).getAsJsonObject();

                String serverId = jsonMessage.get("server").getAsString();

                if (serverId.equals(RedisMessenger.this.serverId)) {
                    return; // same server
                }

                JsonElement object = jsonMessage.get("object");

                Object deserializeObject = gson.fromJson(object, channelObject.type().getType());

                channelObject.callListeners(serverId, deserializeObject);
            }
        };
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> Channel<T> getChannel(String name, TypeToken<T> type) {
        RedisChannel<T> channel = (RedisChannel<T>) channels.get(name);

        if (channel == null) {
            channel = new RedisChannel<>(name, type, this, messengerPool, serverId, gson);

            channels.put(name, channel);

            register(name);
        } else {
            if (!channel.type().equals(type)) {
                throw new IllegalArgumentException("The channel " + name + " is already registered with the type " + type.toString());
            }
        }

        return channel;
    }

    private void register(String channel) {
        if (pubSub == null) {
            return;
        }

        if (pubSub.isSubscribed()) {
            pubSub.subscribe(channel);

            return;
        }

        new Thread(() -> listenerConnection.subscribe(pubSub, channel)).start();

    }

    @Override
    public void close() throws IOException {
        channels.clear();

        if (pubSub.isSubscribed()) {
            pubSub.unsubscribe();
        }
    }
}
