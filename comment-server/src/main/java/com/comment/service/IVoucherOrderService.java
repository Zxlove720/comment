package com.comment.service;

import com.comment.dto.Result;
import com.comment.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Long seckillVoucher(Long voucherId);
}
