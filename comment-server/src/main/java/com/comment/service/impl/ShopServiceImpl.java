package com.comment.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.comment.constant.ErrorConstant;
import com.comment.constant.ShopConstant;
import com.comment.entity.Shop;
import com.comment.mapper.ShopMapper;
import com.comment.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.comment.utils.cache.CacheClient;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private HttpServletResponse httpServletResponse;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    @Resource
    private CacheClient cacheClient;

    /**
     * 根据id查询店铺信息
     *
     * @param id 店铺id
     * @return Result
     */
    @Override
    public Shop queryShopById(Long id) {
        return cacheClient.queryWithCachePierce(ShopConstant.SHOP_CACHE_KEY, id, Shop.class,
                ShopConstant.SHOP_CACHE_TTL, TimeUnit.SECONDS, this::queryShopById);
    }

//    public Shop queryShopById(Long id) {
//        String key = ShopConstant.SHOP_CACHE_KEY + id;
//        // 1.先从Redis中获取店铺信息
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        if (StrUtil.isBlank(shopJson)) {
//            // 1.2如果Redis中没有对应缓存，直接返回空
//            return null;
//        }
//        // 2.缓存命中，判断是否过期
//        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
//        LocalDateTime expireTime = redisData.getExpireTime();
//        // 此时取出的data其实是JsonObject类型，所以说需要先转换，然后再用JsonUtil封装
//        JSONObject shopObject = (JSONObject)redisData.getData();
//        Shop shopCache = JSONUtil.toBean(shopObject, Shop.class);
//        if (expireTime.isAfter(LocalDateTime.now())) {
//            // 2.2缓存没有过期，直接返回
//            return shopCache;
//        }
//        // 3.缓存过期，需要进行重建
//        // 3.1获取互斥锁
//        if (!tryLock(ShopConstant.SHOP_LOCK_KEY + id)) {
//            // 3.2获取锁失败，直接返回
//            return shopCache;
//        }
//        // 3.3获取锁成功，通过线程池开启新线程重建
//        Shop shop = getById(id);
//        // 3.4封装逻辑过期时间
//        RedisData redisData1 = new RedisData(LocalDateTime.now().plusSeconds(10), shop);
//        // 3.5将其写入Redis
//        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData1));
//        return shop;
//    }

    /**
     * 修改店铺信息
     *
     * @param shop 店铺
     */
    @Override
    @Transactional
    public void updateShop(Shop shop) {
        // 1.获取店铺id并判断是否存在
        Long shopId = shop.getId();
        if (shopId == null) {
            // 1.2店铺id为null，抛出异常
            throw new RuntimeException(ErrorConstant.SHOP_ID_IS_NULL);
        }
        // 2.更新店铺信息
        updateById(shop);
        // 3.删除缓存
        stringRedisTemplate.delete(ShopConstant.SHOP_CACHE_KEY + shopId);
    }
}
