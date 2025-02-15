package com.comment.mapper;

import com.comment.entity.SeckillVoucher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 Mapper 接口
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {

}
