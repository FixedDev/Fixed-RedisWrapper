package me.fixeddev.fixedredis.messenger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Set;

public class RedisChannel<T> implements Channel<T> {

    private String serverId;

    private String name;
    private TypeToken<T> type;

    private Messenger messenger;
    private JedisPool jedisPool;
    private Set<ChannelListener<T>> listeners;

    private Gson gson;

    public RedisChannel(String name,
                        TypeToken<T> type,
                        Messenger messenger,
                        JedisPool jedisPool,
                        String serverId,
                        Gson gson) {
        this.name = name;
        this.type = type;

        this.serverId = serverId;

        this.messenger = messenger;
        this.jedisPool = jedisPool;

        this.gson = gson;

        this.listeners = new HashSet<>();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Messenger messenger() {
        return messenger;
    }

    @Override
    public TypeToken<T> type() {
        return type;
    }

    @Override
    public Channel<T> sendMessage(T object) {
        JsonElement serializedObject = gson.toJsonTree(object, type.getType());

        JsonObject objectToSend = new JsonObject();

        objectToSend.addProperty("server", serverId);
        objectToSend.add("object", serializedObject);

        callListenersSend(object);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(name(), objectToSend.toString());
        }

        return this;
    }

    @Override
    public Channel<T> addListener(ChannelListener<T> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }

        return this;
    }

    @Override
    public Channel<T> removeListener(ChannelListener<T> listener) {
        while (listeners.contains(listener)) {
            listeners.remove(listener);
        }

        return this;
    }

    @Override
    public Set<ChannelListener<T>> listeners() {
        return listeners;
    }

    public void callListeners(String server, T object) {
        for (ChannelListener<T> listener : listeners) {
            listener.listen(this, server, object);
        }
    }

    public void callListenersSend(T object) {
        for (ChannelListener<T> listener : listeners) {
            listener.send(this, object);
        }
    }
}
