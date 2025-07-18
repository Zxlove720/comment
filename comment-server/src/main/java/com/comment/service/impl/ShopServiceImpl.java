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
        // 1.先从Redis中获取店铺信息
        String shopJson = stringRedisTemplate.opsForValue().get(ShopConstant.SHOP_CACHE_KEY + id);
        if (StrUtil.isNotBlank(shopJson)) {
            // 1.2如果Redis中有对应缓存，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 此时shopJson只能是null或者空串
        if (shopJson != null) {
            // 1.3shopJson是空串，是解决缓存穿透缓存的空值
            throw new RuntimeException(ErrorConstant.SHOP_NOT_FOUND);
        }
        // 2.此时Redis中没有对应缓存，需要查询数据库
        // 2.1获取互斥锁
        if (!tryLock(ShopConstant.SHOP_LOCK_KEY + id)) {
            // 2.2获取互斥锁失败，等待缓存重建，然后再次查询Redis
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return queryShopById(id);
        }
        // 2.3获取锁成功，先判断Redis中此时是否有缓存，避免重复更新
        Shop shopCheck = doubleCheck(ShopConstant.SHOP_CACHE_KEY + id);
        if (shopCheck != null) {
            return shopCheck;
        }
        Shop shop = getById(id);
        if (shop == null) {
            // 2.1如果数据库中没有对应店铺信息，则将其缓存为空值解决缓存穿透
            stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_CACHE_KEY + id, "",
                    ShopConstant.SHOP_NULL_TTL, TimeUnit.MINUTES);
        }
        // 3.数据库中有对应的商户信息，需要将其加入Redis
        stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_CACHE_KEY + id, JSONUtil.toJsonStr(shop),
                ShopConstant.SHOP_CACHE_TTL, TimeUnit.MINUTES);
        // 4.缓存重建完毕，释放锁
        unLock(ShopConstant.SHOP_LOCK_KEY + id);
        // 5.返回商户信息
        return shop;
    }

    private Shop doubleCheck(String key) {
        return JSONUtil.toBean(stringRedisTemplate.opsForValue().get(key), Shop.class);
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
