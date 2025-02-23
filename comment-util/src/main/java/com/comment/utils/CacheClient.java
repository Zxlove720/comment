package com.comment.utils;

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
 * @author wzb
 */
@Slf4j
@Component
public class CacheClient {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private HttpServletResponse httpServletResponse;

    /**
     * Java对象存储到Redis
     * @param key 键
     * @param value 值
     * @param time 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * Java对象存储到Redis并添加逻辑过期时间
     * @param key 键
     * @param value 值
     * @param time 过期时间
     * @param unit 时间单位
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 指定key查询缓存，并解决缓存穿透问题
     * @param keyPrefix 键前缀
     * @param id id
     * @param type 缓存的实体类类型
     * @param dbFallback 数据库操作函数
     * @param time 过期时间
     * @param unit 时间单位
     * @return R
     * @param <R> 未知类型的实体类
     * @param <ID> 未知类型的id
     */
    public <R,ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit
    ) {
        // 通过key前缀和id拼接完整key
        String key = keyPrefix + id;
        // 从redis中查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 判断缓存是否存在
        if (StrUtil.isNotBlank(json)) {
            // 缓存存在，直接返回
            return JSONUtil.toBean(json, type);
        }
        // 判断命中的缓存是否为空值
        if (json != null) {
            // 返回错误信息
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        // 缓存存在，根据id查询数据库
        R r = dbFallback.apply(id);
        // 数据库不存在，返回错误
        if (r == null) {
            // 将空值写入redis做为缓存
            this.set(key, "", ShopConstant.SHOP_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        // 数据库存在，写入redis作为缓存
        this.set(key, r, time, unit);
        // 返回数据库查询的数据
        return r;
    }

    // 创建线程池
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 指定key查询缓存，使用逻辑过期解决缓存击穿问题
     * @param keyPrefix 键前缀
     * @param id id
     * @param type 缓存的实体类类型
     * @param dbFallback 数据库操作函数
     * @param time 过期时间
     * @param unit 时间单位
     * @return R
     * @param <R> 未知类型的实体类
     * @param <ID> 未知类型的id
     */
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit
    ) {
        String key = keyPrefix + id;
        // 从redis查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 判断缓存是否存在
        if (StrUtil.isBlank(json)) {
            // 不存在，直接返回
            return null;
        }
        // 缓存存在，反序列化为对象后获取数据和过期时间
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回店铺信息
            return r;
        }
        // 已过期，需要缓存重建
        // 缓存重建
        String lockKey = ShopConstant.SHOP_LOCK_KEY + id;
        // 获取互斥锁
        boolean lock = tryLock(lockKey);
        if (lock) {
            // 获取锁成功，开启新的独立线程进行缓存重建
            executor.submit(() -> {
               try {
                   // 缓存重建
                   // 查询数据库
                   R newR = dbFallback.apply(id);
                   // 重建缓存
                   this.setWithLogicalExpire(key, newR, time, unit);
               } catch (Exception e) {
                   throw new RuntimeException();
               } finally {
                   // 释放锁
                   unlock(lockKey);
               }
            });
        }
        // 获取锁失败，临时返回过期的商铺信息
        return r;
    }

    /**
     * 获取锁
     *
     * @param key 锁
     * @return boolean
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     *
     * @param key 锁
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
