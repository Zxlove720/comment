package com.comment.service.impl;

import com.comment.constant.ErrorConstant;
import com.comment.entity.SeckillVoucher;
import com.comment.entity.VoucherOrder;
import com.comment.mapper.VoucherOrderMapper;
import com.comment.service.ISeckillVoucherService;
import com.comment.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.comment.utils.UserHolder;
import com.comment.utils.id.GlobalIDCreator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  优惠券秒杀服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private GlobalIDCreator globalIDCreator;

    /**
     * 优惠券秒杀
     * @param voucherId 优惠券id
     */
    @Override
    public Long seckillVoucher(Long voucherId) {
        // 1.查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 秒杀未开始，返回错误
            throw new RuntimeException(ErrorConstant.VOUCHER_NOT_START);
        }
        // 3.判断秒杀是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 秒杀已经结束，返回错误
            throw new RuntimeException(ErrorConstant.VOUCHER_IS_END);
        }
        // 4.判断库存是否充足
        if (voucher.getStock() < 1) {
            // 库存不足，返回错去
            throw new RuntimeException(ErrorConstant.VOUCHER_IS_SOLD_OUT);
        }
        // 5.如果在秒杀时间内且库存充足，则扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).update();
        if (!success) {
            // 修改库存失败，返回错误
            throw new RuntimeException(ErrorConstant.VOUCHER_IS_SOLD_OUT);
        }
        // 6.购买成功，创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 订单id
        long orderId = globalIDCreator.getGlobalID("order");
        voucherOrder.setId(orderId);
        // 下单用户id
        voucherOrder.setUserId(UserHolder.getUser().getId());
        // 优惠券id
        voucherOrder.setVoucherId(voucherId);
        // 保存订单到数据库
        save(voucherOrder);
        return orderId;
    }
}
