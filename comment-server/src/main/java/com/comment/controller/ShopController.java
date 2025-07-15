package com.comment.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.comment.dto.Result;
import com.comment.entity.Shop;
import com.comment.service.IShopService;
import com.comment.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * <p>
 * 店铺相关controller
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    /**
     * 根据id查询店铺信息
     * @param id 店铺id
     * @return 店铺详情数据
     */
    @GetMapping("/{id}")
    public Result<Shop> queryShopById(@PathVariable("id") Long id) {
        Shop shop = shopService.queryShopById(id);
        return Result.ok(shop);
    }

    /**
     * 新增店铺信息
     * @param shop 店铺数据
     * @return 店铺id
     */
    @PostMapping
    public Result<Long> saveShop(@RequestBody Shop shop) {
        // 写入数据库
        shopService.save(shop);
        // 返回店铺id
        return Result.ok(shop.getId());
    }

    /**
     * 更新店铺信息
     * @param shop 店铺数据
     * @return Result
     */
    @PutMapping()
    public Result updateShop(@RequestBody Shop shop) {
        return shopService.updateShop(shop);
    }

    /**
     * 根据店铺类型分页查询店铺信息
     * @param typeId 店铺类型
     * @param current 页码
     * @return 店铺列表
     */
    @GetMapping("/of/type")
    public Result queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Shop> page = shopService.query()
                .eq("type_id", typeId)
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }

    /**
     * 根据店铺名称关键字分页查询店铺信息
     * @param name 店铺名称关键字
     * @param current 页码
     * @return 店铺列表
     */
    @GetMapping("/of/name")
    public Result queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }
}
