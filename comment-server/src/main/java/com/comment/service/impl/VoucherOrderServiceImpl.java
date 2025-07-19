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
 * 优惠券秒杀服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-7-1
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
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 2.1此时秒杀仍未开始
            throw new RuntimeException(ErrorConstant.VOUCHER_NOT_START);
        }
        // 3.判断秒杀是否结束
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 3.1此时秒杀已经结束
            throw new RuntimeException(ErrorConstant.VOUCHER_IS_END);
        }
        // 4.此时处于秒杀时段内，判断库存是否充足
        if (seckillVoucher.getStock() < 1) {
            // 4.1此时库存不足
            throw new RuntimeException(ErrorConstant.VOUCHER_IS_SOLD_OUT);
        }
        // 5.如果在秒杀时间内且库存充足，则扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).update();
        if (!success) {
            // 5.1修改库存失败，终止业务
            throw new RuntimeException(ErrorConstant.VOUCHER_IS_SOLD_OUT);
        }
        // 6.购买成功，创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 6.1创建订单id
        long orderId = globalIDCreator.getGlobalID("order");
        voucherOrder.setId(orderId);
        // 6.2创建用户id
        voucherOrder.setUserId(UserHolder.getUser().getId());
        // 6.3创建优惠券id
        voucherOrder.setVoucherId(voucherId);
        // 6.4将订单保存至数据库
        save(voucherOrder);
        // 7.返回订单id
        return orderId;
    }
}
