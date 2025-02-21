package com.comment.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.comment.constant.ErrorConstant;
import com.comment.constant.ShopConstant;
import com.comment.dto.Result;
import com.comment.entity.Shop;
import com.comment.mapper.ShopMapper;
import com.comment.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
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
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private HttpServletResponse httpServletResponse;

    /**
     * 根据id查询店铺信息
     *
     * @param id 店铺id
     * @return Result
     */
    @Override
    public Result queryShopById(Long id) {
        // 从缓存中查询店铺
        String shopJson = stringRedisTemplate.opsForValue().get(ShopConstant.SHOP_CACHE_KEY + id);
        if (StrUtil.isNotBlank(shopJson)) {
            if (shopJson.equals("null")) {
                // 缓存的店铺信息无效，直接返回错误
                httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return Result.ok(ErrorConstant.SHOP_NOT_FOUND);
            }
            // 命中缓存，直接返回
            Shop shopCache = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shopCache);
        }
        // 获取互斥锁
        String lockKey = ShopConstant.SHOP_LOCK_KEY + id;
        Shop shop = null;
        try {
            boolean lock = tryLock(lockKey);
            // 判断获取锁是否成功
            if (!lock) {
                // 获取锁失败，休眠重试
                Thread.sleep(50);
                return queryShopById(id);
            }
            // 获取锁成功，从数据库中查询
            shop = getById(id);
            if (shop == null) {
                // 如果从数据库中查询失败，返回错误信息，并将空值加入缓存
                stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_CACHE_KEY + id, "null");
                httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return Result.fail(ErrorConstant.SHOP_NOT_FOUND);
            }
            // 查询成功，则将其加入缓存
            stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_CACHE_KEY + id, JSONUtil.toJsonStr(shop));
            // 设置过期时间
            // 过期时间修改为随机值，解决缓存雪崩问题
            stringRedisTemplate.expire(ShopConstant.SHOP_CACHE_KEY + id, RandomUtil.randomInt(15, 31), TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            // 释放锁
            unlock(lockKey);
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
