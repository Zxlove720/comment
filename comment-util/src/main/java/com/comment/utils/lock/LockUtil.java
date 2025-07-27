package com.comment.utils.lock;

import cn.hutool.core.lang.UUID;
import com.comment.constant.LockConstant;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 锁工具类
 */
public class LockUtil {

    private final StringRedisTemplate stringRedisTemplate;

    // 业务名
    private final String name;

    // 线程id前缀
    private static final String THREAD_PREFIX = UUID.randomUUID().toString(true);

    // 释放锁的LUA脚本
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("script/unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

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
        // 调用LUA脚本释放锁，解决原子性问题
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(LockConstant.LOCK_PREFIX + name),
                THREAD_PREFIX + "-" + Thread.currentThread().getId()
        );
    }
}
