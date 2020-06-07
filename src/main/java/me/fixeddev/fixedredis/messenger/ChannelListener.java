package me.fixeddev.fixedredis.messenger;

public interface ChannelListener<T> {
    void listen(Channel<T> channel, String server, T object);

    default void send(Channel<T> channel, T object){}
}
