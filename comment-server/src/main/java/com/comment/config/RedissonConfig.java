package com.comment.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        // 1.创建Redisson配置类
        Config config = new Config();
        // 2.添加Redis单节点地址和密码
        config.useSingleServer().setAddress("redis://8.137.37.221:6379").setPassword("262460wzbWZB...");
        // 3.创建Redisson客户端返回
        return Redisson.create(config);
    }

}
