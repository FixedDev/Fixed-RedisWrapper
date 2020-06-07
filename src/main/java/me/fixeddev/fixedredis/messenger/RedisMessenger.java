package me.fixeddev.fixedredis.messenger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class RedisMessenger implements Messenger {

    private String serverId;
    private Plugin plugin;
    private Gson gson;

    private JedisPool messengerPool;
    private Jedis listenerConnection;

    private Map<String, RedisChannel<?>> channels;

    private JedisPubSub pubSub;
    private JsonParser parser = new JsonParser();

    private boolean closed = false;

    public RedisMessenger(JedisPool jedisPool, Jedis listenerConnection, Plugin plugin, String serverId, Gson gson) {
        this.serverId = serverId;
        this.plugin = plugin;
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

        new BukkitRunnable() {

            @Override
            public void run() {
                while (!closed) {
                    listenerConnection.subscribe(pubSub,channels.keySet().toArray(new String[0]) );
                }
            }
        }.runTaskAsynchronously(plugin);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> Channel<T> getChannel(String name, TypeToken<T> type) {
        RedisChannel<T> channel = (RedisChannel<T>) channels.get(name);

        if (channel == null) {
            channel = new RedisChannel<>(name, type, this, messengerPool, serverId, gson);

            channels.put(name, channel);

            register();
        } else {
            if (!channel.type().equals(type)) {
                throw new IllegalArgumentException("The channel " + name + " is already registered with the type " + type.toString());
            }
        }

        return channel;
    }

    private void register() {
        if (pubSub != null) {
            if(pubSub.isSubscribed()){
                pubSub.unsubscribe();
            }

            return;
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        channels.clear();

        if(pubSub.isSubscribed()){
            pubSub.unsubscribe();
        }
    }
}
