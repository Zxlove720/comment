package com.comment.service;

import com.comment.dto.Result;
import com.comment.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
public interface IVoucherService extends IService<Voucher> {

    List<Voucher> queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
