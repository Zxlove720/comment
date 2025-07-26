package com.comment.utils.lock;

import com.comment.constant.LockConstant;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class LockUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private final String name;

    public LockUtil(StringRedisTemplate stringRedisTemplate, String name) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.name = name;
    }

    public boolean tryLock(long timeOut) {
        // 1.获取线程id，确保每个线程只能释放自己的锁
        long id = Thread.currentThread().getId();
        // 2.尝试获取锁
        String key = LockConstant.LOCK_PREFIX + name;
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(key, id + "", timeOut, TimeUnit.SECONDS);
        // 3.判断获取锁是否成功
        return Boolean.TRUE.equals(lock);
    }
}
