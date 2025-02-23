package com.comment.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.comment.constant.ShopConstant;
import com.comment.entity.RedisData;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存工具类
 * @author wzb
 */
@Slf4j
@Component
public class CacheUtils {

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


}
