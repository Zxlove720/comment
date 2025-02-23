package com.comment.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.comment.constant.ErrorConstant;
import com.comment.constant.ShopConstant;
import com.comment.dto.Result;
import com.comment.entity.RedisData;
import com.comment.entity.Shop;
import com.comment.mapper.ShopMapper;
import com.comment.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.comment.utils.CacheClient;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 店铺服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private HttpServletResponse httpServletResponse;
    @Resource
    private CacheClient cacheClient;

    /**
     * 根据id查询店铺信息
     *
     * @param id 店铺id
     * @return Result
     */
    @Override
    public Result queryShopById(Long id) {
        // 解决缓存穿透
        Shop shop = cacheClient.queryWithPassThrough(ShopConstant.SHOP_CACHE_KEY, id, Shop.class,
                this::getById, ShopConstant.SHOP_CACHE_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return Result.fail(ErrorConstant.SHOP_NOT_FOUND);
        }
        return Result.ok(shop);
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

    // 创建线程池
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 使用逻辑过期解决缓存击穿
     *
     * @param id 店铺id
     * @return Result
     */
    public Result queryShopWithLogicalExpire(Long id) {
        String key = ShopConstant.SHOP_CACHE_KEY + id;
        // 从redis查询店铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(shopJson)) {
            // 不存在该缓存，直接返回
            return Result.fail(ErrorConstant.SHOP_NOT_FOUND);
        }
        // 存在该键，返回缓存
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        // 将RedisData中数据转换为对应的bean
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        // 获取过期时间
        LocalDateTime expireTime = redisData.getExpireTime();
        // 判断该缓存是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回
            return Result.ok(shop);
        }
        // 该缓存已过期，需要重建
        String lockKey = ShopConstant.SHOP_LOCK_KEY + id;
        // 获取锁
        boolean lock = tryLock(lockKey);
        if (lock) {
            // 获取锁成功
            // 开启新的线程重建缓存
            executor.submit(() -> {
                try {
                    this.saveShop2Redis(id, RandomUtil.randomLong(15, 31));
                } catch (Exception e) {
                    throw new RuntimeException();
                } finally {
                    unlock(lockKey);
                }
            });
        }
        // 返回过期店铺信息
        return Result.ok(shop);
    }

    /**
     * 缓存重建
     *
     * @param id 店铺id
     * @param expireSecond 过期时间
     */
    private void saveShop2Redis(Long id, Long expireSecond) {
        // 查询店铺数据
        Shop shop = getById(id);
        // 封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSecond));
        // 重新写入redis
        stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_CACHE_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 修改店铺信息
     *
     * @param shop 店铺
     * @return Result
     */
    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        // 获取店铺id
        Long id = shop.getId();
        if (id == null) {
            return Result.fail(ErrorConstant.SHOP_NOT_FOUND);
        }
        // 更新数据库
        updateById(shop);
        // 删除缓存
        stringRedisTemplate.delete(ShopConstant.SHOP_CACHE_KEY + id);
        return Result.ok();
    }
}
