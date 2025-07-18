package com.comment.service;

import com.comment.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 店铺服务类
 * </p>
 *
 * @author wzb
 * @since 2025-7-1
 */
public interface IShopService extends IService<Shop> {

    Shop queryShopById(Long id);

    void updateShop(Shop shop);
}
