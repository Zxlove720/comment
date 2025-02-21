package com.comment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Redis存储value数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisData {
    // 该键过期时间
    private LocalDateTime expireTime;
    // 该键对应的实体类
    private Object data;
}
