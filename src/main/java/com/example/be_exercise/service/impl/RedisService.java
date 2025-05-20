package com.example.be_exercise.service.impl;

import com.example.be_exercise.service.IRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService<K, V> implements IRedisService<K, V> {
    private final RedisTemplate<K, V> redisTemplate;

    @Override
    public V get(K key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void set(K key, V value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setTimeToLive(K key, Long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
    }

    @Override
    public void delete(K key) {
        redisTemplate.delete(key);
    }
    
    @Override
    public void flushDb() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    // For forgot password
    public Long incrementRequestCount(K key) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            setTimeToLive(key, 5L);
        }
        return count;
    }

    public boolean isRequestLimitExceeded(K key, int maxAttempts) {
        Long count = (Long) redisTemplate.opsForValue().get(key);
        return count > maxAttempts;
    }
}
