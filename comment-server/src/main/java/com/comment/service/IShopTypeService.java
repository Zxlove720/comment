package com.comment.service;

import com.comment.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  店铺类型Service
 * </p>
 *
 * @author wzb
 * @since 2025-7-1
 */
public interface IShopTypeService extends IService<ShopType> {

    List<ShopType> getShopType();

}
