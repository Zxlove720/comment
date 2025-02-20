package com.comment.service.impl;

import cn.hutool.json.JSONUtil;
import com.comment.constant.ShopConstant;
import com.comment.dto.Result;
import com.comment.entity.Shop;
import com.comment.mapper.ShopMapper;
import com.comment.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Result queryShopById(Long id) {
        // 从缓存中查询商户
        String shopJson = stringRedisTemplate.opsForValue().get(ShopConstant.SHOP_CACHE_KEY + id);
        Shop shop = JSONUtil.toBean(shopJson, Shop.class);
        if (shop != null) {
            // 命中缓存，直接返回
            return Result.ok(shop);
        }
        // 缓存未命中，从数据库中查询



        return Result.ok();
    }
}
