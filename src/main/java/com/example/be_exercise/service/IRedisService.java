package com.example.be_exercise.service;

public interface IRedisService<K, V> {
    V get(K key);
    void set(K key, V value);
    void setTimeToLive(K key, Long timeout);
    void delete(K key);
    void flushDb();
}
