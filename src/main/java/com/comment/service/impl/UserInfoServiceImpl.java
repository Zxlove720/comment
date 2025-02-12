package com.comment.service.impl;

import com.comment.entity.UserInfo;
import com.comment.mapper.UserInfoMapper;
import com.comment.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
