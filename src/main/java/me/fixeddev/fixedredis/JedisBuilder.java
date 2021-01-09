package me.fixeddev.fixedredis;

import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.logging.Logger;

public class JedisBuilder {
    private Jedis jedis;
    private JedisPool jedisPool;

    private String host = "localhost";
    private int port = 6379;
    private String password = "";

    private int timeout = 2000;

    private JedisPoolConfig config = new JedisPoolConfig();

    JedisBuilder() {
    }

    public JedisBuilder setHost(String host) {
        Preconditions.checkNotNull(host);
        this.host = host;

        return this;
    }

    public JedisBuilder setPort(int port) {
        this.port = port;

        return this;
    }

    public JedisBuilder setPassword(String password) {
        this.password = password;

        return this;
    }

    public JedisBuilder setTimeout(int timeout) {
        this.timeout = timeout;

        return this;
    }

    public JedisBuilder setConfig(JedisPoolConfig config) {
        Preconditions.checkNotNull(config);
        this.config = config;

        return this;
    }

    public JedisResult build() {
        startRedis();

        return new JedisResult() {
            @Override
            public Jedis listenerConnection() {
                return jedis;
            }

            @Override
            public JedisPool pool() {
                return jedisPool;
            }
        };
    }

    private void startRedis() {
        jedis = new Jedis(host, port, timeout);

        if (password == null || password.isEmpty()) {
            this.jedisPool = new JedisPool(config, host, port, timeout);
        } else {
            this.jedisPool = new JedisPool(config, host, port, timeout, password);
            jedis.auth(password);
        }
    }

    public static JedisBuilder builder() {
        return new JedisBuilder();
    }

    public static JedisBuilder fromConfig(ConfigurationSection section) {
        return new JedisBuilder()
                .setHost(section.getString("host", "localhost"))
                .setPort(section.getInt("port", 6379))
                .setPassword(section.getString("password", ""))
                .setTimeout(section.getInt("timeout", 2000));
    }
}
