package com.comment.controller;


import com.comment.dto.Result;
import com.comment.service.ISeckillVoucherService;
import com.comment.service.IVoucherOrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 优惠券订单相关Controller
 * </p>
 *
 * @author wzb
 * @since 2025-7-1
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @PostMapping("seckill/{id}")
    public Result<Long> seckillVoucher(@PathVariable("id") Long voucherId) {
        return Result.ok(voucherOrderService.seckillVoucher(voucherId));
    }
}
