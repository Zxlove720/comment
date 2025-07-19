package com.comment.service;

import com.comment.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 优惠券秒杀服务类
 * </p>
 *
 * @author wzb
 * @since 2025-7-1
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Long seckillVoucher(Long voucherId);

}
