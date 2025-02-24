package com.comment.service.impl;

import com.comment.constant.ErrorConstant;
import com.comment.constant.ShopConstant;
import com.comment.dto.Result;
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
     * 修改店铺信息
     * @param shop 店铺
     * @return Result
     */
    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        // 获取店铺id
        Long id = shop.getId();
        if (id == null) {
            log.info(ErrorConstant.SHOP_NOT_FOUND);
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return Result.fail(ErrorConstant.SHOP_NOT_FOUND);
        }
        // 更新数据库
        updateById(shop);
        log.info(ShopConstant.SHOP_UPDATE_SUCCESSFULLY);
        // 删除缓存
        stringRedisTemplate.delete(ShopConstant.SHOP_CACHE_KEY + id);
        return Result.ok();
    }
}
