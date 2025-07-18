package com.comment;

import cn.hutool.json.JSONUtil;
import com.comment.constant.ShopConstant;
import com.comment.entity.RedisData;
import com.comment.entity.Shop;
import com.comment.service.impl.ShopServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

@SpringBootTest
public class LogicalExpireTests {

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void cacheTest() {
        Shop shop = shopService.getById(1);
        RedisData redisData = new RedisData(LocalDateTime.now().plusSeconds(60), shop);
        stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_CACHE_KEY + "1", JSONUtil.toJsonStr(redisData));
    }
}
