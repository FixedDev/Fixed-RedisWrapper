# Fixed-RedisWrapper
A functional style wrapper for jedis that allows easy creation of jedis instances, also, includes a Messenger based on Jedis and Gson
## Usage
### JedisBuilder
First at all you need to create your jedis instance using the JedisBuilder

You can use`JedisBuilder.builder()`and build the jedis instance manually or use
`JedisBuilder.fromConfig(ConfigurationSection)`and build it using the Bukkit Configuration API
### Redis
Now, having your JedisBuilder instance created, you can create a Redis instance(just a class name, not actual redis).

```java
Plugin plugin = // Your plugin;
Redis.builder(plugin)
      .jedis(jedisBuilder);
      .build();
```

Also, you can set other things like the gson instance to use, the server id for the messager, or the actual JedisPool and Jedis listener connection to use 
### Messenger
After you created the Redis instance a Messenger instance was created, you can use it by `redis.messenger()`. That method returns a valid messenger instance.
Everything from send messages, to add listeners is done on a channel instance which wraps the message sending an receiving 

Now, you can get a channel instance with `messenger.getChannel(name, class)` or instead of class you can use a gson TypeToken to allow for generic types
### Channel
The channel is a generic type, why? Because we use Gson, that means that the channel will automatically serialize your message and send it as a json representation.

A channel has 2 basic methods `sendMessage(T)` and `addListener(ChannelListener<T>)`. When you send a message it is serialized and send as a json in a normal redis channel.
When you receive it using a ChannelListener it will be automatically deserialized.
### ChannelListener
It's only an interface with 2 methods, one to listen when a message is received(`listen(Channel, Server, Message)`) and other one to listen when a message is send(`send(Channel, Message)`)
