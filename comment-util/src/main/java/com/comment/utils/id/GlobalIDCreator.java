package com.comment.utils.id;


import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 全局唯一ID生成器
 */
@Component
public class GlobalIDCreator {

    // 开始时间戳
    private static final long BEGIN_TIMESTAMP = 1097712000L;

    // 序列号的位数
    private static final int COUNT_BITS = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 全局唯一ID生成器
     * 通过31位时间戳 + 32位序列号 + 1符号位拼接成的long类型的全局唯一ID
     * @param keyPrefix 键前缀
     * @return 全局唯一ID
     */
    public long nextId(String keyPrefix) {
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        // 获取现在时间的时间戳
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        // 通过现在时间的时间戳和开始时间戳的差生成时间戳
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;

        // 2.生成序列号
        // 获取当前日期，精确到天，每一天的键不同
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 序列号自增
        long number = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3.时间戳和序列号进行拼接，成为全局id返回
        return timeStamp << COUNT_BITS | number;
    }
}
