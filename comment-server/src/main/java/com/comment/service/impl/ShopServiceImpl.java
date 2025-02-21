package com.comment.service.impl;

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

/**
 * <p>
 * 商户服务类
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


    @Override
    public Result queryShopById(Long id) {
        // 从缓存中查询商户
        String shopJson = stringRedisTemplate.opsForValue().get(ShopConstant.SHOP_CACHE_KEY + id);
        if (StrUtil.isNotBlank(shopJson)) {
            // 命中缓存，直接返回
            Shop shopCache = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shopCache);
        }
        // 缓存未命中，从数据库中查询
        Shop shop = getById(id);
        if (shop == null) {
            // 如果从数据库中查询失败，返回错误信息
            httpServletResponse.setStatus(404);
            return Result.fail(ErrorConstant.SHOP_NOT_FOUND);
        }
        // 查询成功，则将其加入缓存
        stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_CACHE_KEY + id, JSONUtil.toJsonStr(shop));
        // 返回商户信息
        return Result.ok(shop);
    }
}
