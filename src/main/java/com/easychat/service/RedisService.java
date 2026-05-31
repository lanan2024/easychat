package com.easychat.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {
    void set(String key, Object value);
    void setWithExpire(String key, Object value, long timeout, TimeUnit unit);
    <T> T get(String key, Class<T> clazz);
    void delete(String key);
    boolean hasKey(String key);
}
