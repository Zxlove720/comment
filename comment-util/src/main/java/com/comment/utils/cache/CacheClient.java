package com.comment.utils.cache;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.comment.constant.ShopConstant;
import com.comment.entity.RedisData;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存工具类
 *
 * @author wzb
 */
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 添加缓存
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param time     过期时间
     * @param timeUnit 时间单位
     */
    public void addCache(String key, Object value, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, timeUnit);
    }

    /**
     * 添加缓存并使用逻辑过期
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param time     过期时间
     * @param timeUnit 时间单位
     */
    public void addCacheLogical(String key, Object value, Long time, TimeUnit timeUnit) {
        RedisData redisData = new RedisData();
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        redisData.setData(value);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 缓存穿透并使用互斥锁解决缓存击穿
     *
     * @param keyPrefix 缓存键前缀
     * @param suffix 缓存键后缀（一般为id）
     * @param type 返回类型
     * @param time 缓存有效期
     * @param timeUnit 时间单位
     * @param dbQuery 数据库查询函数
     * @return R
     * @param <R> 返回实体类型
     * @param <T> 不同类型的缓存后缀
     */
    public <R, T> R queryWithCachePierce(String keyPrefix, T suffix, Class<R> type, Long time, TimeUnit timeUnit, Function<T, R> dbQuery) {
        String key = keyPrefix + suffix;
        // 1.从Redis中获取缓存
        String s = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(s)) {
            // 1.2如果缓存不为空，直接返回
            return JSONUtil.toBean(s, type);
        }
        if (s != null) {
            // 1.3此时缓存为空字符串，直接返回
            return null;
        }
        R result = null;
        try {
            // 2.缓存不存在，需要查询数据库重建缓存
            if (!tryLock(ShopConstant.SHOP_LOCK_KEY + suffix)) {
                // 2.1获取互斥锁失败，等待并重新查询缓存
                Thread.sleep(50);
                return queryWithCachePierce(keyPrefix, suffix, type, time, timeUnit, dbQuery);
            }
            // 2.1成功获得互斥锁
            // 2.2二次判断缓存重建是否成功
            R cacheResult = doubleCheck(key, type);
            if (cacheResult != null) {
                // 2.3缓存重建成功，直接返回
                return cacheResult;
            }
            // 2.4缓存重建失败，开始缓存重建
            result = dbQuery.apply(suffix);
            if (result == null) {
                // 2.5查询结果为空，则缓存空字符串解决缓存穿透
                stringRedisTemplate.opsForValue().set(key, "", ShopConstant.SHOP_NULL_TTL, TimeUnit.MINUTES);
                unLock(ShopConstant.SHOP_LOCK_KEY + suffix);
                return null;
            }
            // 2.6查询结果不为空，将其接入Redis重建缓存
            this.addCache(key, result, time, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            unLock(ShopConstant.SHOP_LOCK_KEY + suffix);
        }
        return result;
    }

    /**
     * 获取互斥锁
     *
     * @param key 键
     * @return 是否获取到锁
     */
    private boolean tryLock(String key) {
        // 通过setNX命令实现互斥锁
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(key, "lock", ShopConstant.SHOP_LOCK_TTL, TimeUnit.SECONDS);
        // 通过BooleanUtil进行封装返回，避免空指针
        return BooleanUtil.isTrue(lock);
    }

    /**
     * 释放互斥锁
     *
     * @param key 键
     */
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取锁之后的二次判断
     *
     * @param key 键
     * @return Shop 店铺信息
     */
    private <R> R doubleCheck(String key, Class<R> type) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return null;
        } else {
            return JSONUtil.toBean(json, type);
        }

    }
}
