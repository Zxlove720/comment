package com.comment.service.impl;

import cn.hutool.json.JSONUtil;
import com.comment.constant.ShopTypeConstant;
import com.comment.entity.ShopType;
import com.comment.mapper.ShopTypeMapper;
import com.comment.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 店铺类型服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 
     * @return
     */
    @Override
    public List<ShopType> getShopType() {
        // 1.先从缓存中查询店铺类型
        List<String> shopTypeJson = stringRedisTemplate.opsForList().range(ShopTypeConstant.SHOP_TYPE_CACHE, 0, -1);
        List<ShopType> shopTypeList = new ArrayList<>();
        if (shopTypeJson != null && !shopTypeJson.isEmpty()) {
            // 1.2缓存中有店铺类型信息，直接返回
            for (String shop : shopTypeJson) {
                ShopType shopType = JSONUtil.toBean(shop, ShopType.class);
                shopTypeList.add(shopType);
            }
            return shopTypeList;
        }
        // 2.缓存中没有店铺类型，从数据库中查询
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        // 2.1将数据库中查询的信息缓存到Redis中
        for (ShopType shopType : shopTypes) {
            stringRedisTemplate.opsForList().rightPush(ShopTypeConstant.SHOP_TYPE_CACHE, JSONUtil.toJsonStr(shopType));
        }
        return shopTypes;
    }
}
