package com.comment.service;

import com.comment.dto.Result;
import com.comment.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 店铺服务类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
public interface IShopService extends IService<Shop> {

    Result queryShopById(Long id);

    Result updateShop(Shop shop);
}
