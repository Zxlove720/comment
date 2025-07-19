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
    private CacheClient cacheClient;

    /**
     * 根据id查询店铺信息
     *
     * @param id 店铺id
     * @return Shop 店铺实体类
     */
    @Override
    public Shop queryShopById(Long id) {
        return cacheClient.queryWithCachePierce(ShopConstant.SHOP_CACHE_KEY, id, Shop.class,
                ShopConstant.SHOP_CACHE_TTL, TimeUnit.SECONDS, this::getById);
    }

    /**
     * 通过逻辑过期解决缓存击穿并查询店铺信息
     *
     * @param id 店铺id
     * @return Shop 店铺实体类
     */
    public Shop queryShopByIdLogicalExpire(Long id) {
        return cacheClient.queryWithLogicalExpire(ShopConstant.SHOP_CACHE_KEY, id, Shop.class,
                ShopConstant.SHOP_CACHE_TTL, TimeUnit.SECONDS, this::getById);
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
