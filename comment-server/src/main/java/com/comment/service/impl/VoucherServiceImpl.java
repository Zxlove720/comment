package com.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.comment.dto.Result;
import com.comment.entity.Voucher;
import com.comment.mapper.VoucherMapper;
import com.comment.entity.SeckillVoucher;
import com.comment.service.ISeckillVoucherService;
import com.comment.service.IVoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Override
    public List<Voucher> queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        return getBaseMapper().queryVoucherOfShop(shopId);
    }

    /**
     * 新增秒杀优惠券
     * @param voucher 秒杀优惠券
     */
    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存到普通优惠券表
        save(voucher);
        // 保存秒杀信息至秒杀优惠券表
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
    }
}
