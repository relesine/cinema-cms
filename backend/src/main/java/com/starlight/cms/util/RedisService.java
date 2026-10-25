package com.starlight.cms.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 通用 Redis 缓存工具：封装常用读写、过期、删除、setnx 锁操作
 */
@Component
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * setnx：仅当 key 不存在时写入成功（选座防超卖的核心）
     * @return true=抢锁成功
     */
    public boolean setNx(String key, Object value, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit));
    }

    /** 模糊删除（如释放某场次全部锁定座位） */
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
