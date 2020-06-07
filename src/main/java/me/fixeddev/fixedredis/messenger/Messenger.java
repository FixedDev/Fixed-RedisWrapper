package me.fixeddev.fixedredis.messenger;

import com.google.gson.reflect.TypeToken;

import java.io.Closeable;

public interface Messenger extends Closeable {
    <T> Channel<T> getChannel(String name, TypeToken<T> type);

    default <T> Channel<T> getChannel(String name, Class<T> type){
        return getChannel(name, TypeToken.get(type));
    }
}
