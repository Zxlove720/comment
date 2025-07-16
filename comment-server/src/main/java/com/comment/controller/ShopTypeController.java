package com.comment.controller;


import com.comment.dto.Result;
import com.comment.entity.ShopType;
import com.comment.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 店铺类型Controller
 * </p>
 *
 * @author wzb
 * @since 2025-7-1
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result<List<ShopType>> queryTypeList() {
        List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();
        return Result.ok(typeList);
    }
}
