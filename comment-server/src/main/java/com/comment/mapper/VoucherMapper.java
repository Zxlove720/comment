package com.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.comment.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);

}
