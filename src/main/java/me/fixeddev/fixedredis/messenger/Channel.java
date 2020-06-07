package me.fixeddev.fixedredis.messenger;

import com.google.gson.reflect.TypeToken;

import java.util.Set;

public interface Channel<T> {
    String name();

    Messenger messenger();

    TypeToken<T> type();

    Channel<T> sendMessage(T object);

    Channel<T> addListener(ChannelListener<T> listener);

    Channel<T> removeListener(ChannelListener<T> listener);

    Set<ChannelListener<T>> listeners();
}
