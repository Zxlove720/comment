package com.comment.utils.id;


import com.comment.constant.GlobalIDConstant;
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

    // 全局唯一id起始时间戳
    private final Long BEGIN_STAMP = 1097712000L;
    // 序列号位数
    private final Integer COUNT_BITS = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取全局唯一id
     *
     * @param suffix 具体业务后缀
     * @return long 全局唯一id
     */
    public long getGlobalID(String suffix) {
        // 1.获取时间戳
        long nowTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowTime - BEGIN_STAMP;
        // 2.生成序列号
        // 2.1获取当前的日期，以每一天进行分隔
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment(GlobalIDConstant.GLOBAL_ID_KEY + suffix + ":" + date);
        return timeStamp << COUNT_BITS | count;
    }

    /**
     * 获取起始时间戳
     */
    public static void main(String[] args) {
        System.out.println(LocalDateTime.of(2004, 10, 14, 0, 0).toEpochSecond(ZoneOffset.UTC));
    }
}
