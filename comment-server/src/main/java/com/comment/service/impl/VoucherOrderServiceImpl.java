package com.comment.service.impl;

import com.comment.constant.ErrorConstant;
import com.comment.constant.LockConstant;
import com.comment.entity.SeckillVoucher;
import com.comment.entity.VoucherOrder;
import com.comment.mapper.VoucherOrderMapper;
import com.comment.service.ISeckillVoucherService;
import com.comment.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.comment.utils.UserHolder;
import com.comment.utils.id.GlobalIDCreator;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
//TODO解决超卖问题的乐观锁CAS实现和版本号实现是重点
//TODO解决一人一单问题时使用的事务机制是重点

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

    @Resource
    private RedissonClient redissonClient;

    /**
     * 优惠券秒杀-乐观锁解决超卖
     *
     * @param voucherId 优惠券id
     * @return Long 订单id
     */
    @Override
    public Long seckillVoucher(Long voucherId) throws InterruptedException {
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
        // 4.1获取当前库存
        int stock = seckillVoucher.getStock();
        if (stock < 1) {
            // 4.1此时库存不足
            throw new RuntimeException(ErrorConstant.VOUCHER_IS_SOLD_OUT);
        }
        // 5.获取用户id并以此加锁，确保一人一单
        Long userId = UserHolder.getUser().getId();
        // 5.1获取锁
        RLock rLock = redissonClient.getLock(LockConstant.LOCK_PREFIX + "order:");
        boolean lock = rLock.tryLock(1, 10, TimeUnit.SECONDS);
        if (!lock) {
            // 5.2获取锁失败，抛出异常
            throw new RuntimeException(ErrorConstant.VOUCHER_HAS_BEEN_BOUGHT);
        }
        try {
            // 5.3获取锁成功，创建优惠券订单
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            // 5.4业务执行结束或出现问题，释放锁
            rLock.unlock();
        }
    }

    /**
     * 创建优惠券订单
     *
     * @param voucherId 优惠券id
     * @return Long 订单id
     */
    @Transactional
    public Long createVoucherOrder(Long voucherId) {
        // 1.确保一个用户只能购买一单
        Long userId = UserHolder.getUser().getId();
        // 1.1查询用户是否已经购买过该优惠券
        Long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            // 1.2用户已经重复下单，拒绝再次购买
            throw new RuntimeException(ErrorConstant.VOUCHER_HAS_BEEN_BOUGHT);
        }
        // 2.如果在秒杀时间内、库存充足、库存没有改变并且用户重复购买，则扣减库存完成下单
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0).
                update();
        if (!success) {
            // 2.1修改库存失败，终止业务
            throw new RuntimeException(ErrorConstant.VOUCHER_IS_SOLD_OUT);
        }
        // 3.购买成功，创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 3.1创建订单id
        long orderId = globalIDCreator.getGlobalID("order");
        voucherOrder.setId(orderId);
        // 3.2创建用户id
        voucherOrder.setUserId(userId);
        // 3.3创建优惠券id
        voucherOrder.setVoucherId(voucherId);
        // 3.4将订单保存至数据库
        save(voucherOrder);
        // 4.返回订单id
        return orderId;
    }
}
