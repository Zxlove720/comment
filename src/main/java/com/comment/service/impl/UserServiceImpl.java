package com.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.comment.entity.User;
import com.comment.mapper.UserMapper;
import com.comment.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
