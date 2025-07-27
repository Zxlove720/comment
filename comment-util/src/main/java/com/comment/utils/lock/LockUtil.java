package com.comment.utils.lock;

import cn.hutool.core.lang.UUID;
import com.comment.constant.LockConstant;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 锁工具类
 */
public class LockUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private final String name;

    private static final String THREAD_PREFIX = UUID.randomUUID().toString(true);

    public LockUtil(StringRedisTemplate stringRedisTemplate, String name) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.name = name;
    }

    /**
     * 获取锁
     *
     * @param timeOut 锁过期时间
     * @return 是否获取锁成功
     */
    public boolean tryLock(long timeOut) {
        // 1.获取线程id，存入Redis
        String threadId = THREAD_PREFIX + "-" + Thread.currentThread().getId();
        // 2.尝试获取锁
        String key = LockConstant.LOCK_PREFIX + name;
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(key, threadId, timeOut, TimeUnit.SECONDS);
        // 3.判断获取锁是否成功
        return Boolean.TRUE.equals(lock);
    }

    /**
     * 释放锁
     */
    public void unlock() {
        // 1.获取线程id，确保每个线程只能释放自己的锁
        String threadId = THREAD_PREFIX + "-" + Thread.currentThread().getId();
        // 2.判断该线程是否释放的是自己的锁
        String key = LockConstant.LOCK_PREFIX + name;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return;
        }
        if (!threadId.equals(value)) {
            // 2.1当前线程id不等于Redis中的线程id，代表释放的不是同一把锁，直接返回
            return;
        }
        // 2.2线程id等于Redis中的线程id，释放的是同一把锁，可以释放
        stringRedisTemplate.delete(key);
    }
}
