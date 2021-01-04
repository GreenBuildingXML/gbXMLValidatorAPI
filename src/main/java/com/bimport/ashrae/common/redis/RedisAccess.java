package com.bimport.ashrae.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisAccess {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${ashrae.redis.default-expire}")
    private long DEFAULT_EXPIRE;


    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

    public void set(String key, String value, int expireSeconds) {
        redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
    }

    public void zSet(String key, String value, double score){
        redisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<Object> zGetRangeByIndex(String key, long l1, long l2){
        return redisTemplate.opsForZSet().range(key, l1, l2);
    }

    public Set<Object> zGetRangeByScore(String key, double l1, double l2){
        return redisTemplate.opsForZSet().rangeByScore(key, l1, l2);
    }
    public Set<Object> zGetRevRangeByScore(String key, double l1, double l2){
        return redisTemplate.opsForZSet().reverseRangeByScore(key, l1, l2);
    }
    public void zDel(String key, String value){
        redisTemplate.opsForZSet().remove(key, value);
    }

    //    get the unseen notifications size
    public Long zGetSize(String key){
        return redisTemplate.opsForZSet().size(key);
    }

    public Boolean refresh(String key, int expireSeconds) {
        return redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
    }

    public String get(String key) {
        Object read = redisTemplate.opsForValue().get(key);
        return read == null ? null : read.toString();
    }

    public byte[] getBytes(String key) {
        String str = get(key);
        return str == null ? null : str.getBytes(StandardCharsets.UTF_8);
    }

    public void setBytes(String key, byte[] content) {
        redisTemplate.opsForValue().set(key, content, DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

    public void setBytes(String key, byte[] content, int expireSeconds) {
        redisTemplate.opsForValue().set(key, content, expireSeconds, TimeUnit.SECONDS);
    }

    public Boolean del(String key) {
        return redisTemplate.delete(key);
    }

    public Long rpush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public Long lpush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public String rpop(String key) {
        Object read = redisTemplate.opsForList().rightPop(key);
        return read == null ? null : (String) read;
    }

    public String lpop(String key) {
        Object read = redisTemplate.opsForList().leftPop(key);
        return read == null ? null : (String)read;
    }

    public Boolean expire(String key) {
        return del(key);
    }
}
