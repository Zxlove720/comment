package com.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.comment.dto.LoginFormDTO;
import com.comment.dto.Result;
import com.comment.entity.User;
import jakarta.servlet.http.HttpSession;

/**
 * <p>
 * 用户相关服务
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
public interface IUserService extends IService<User> {

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sendCode(String phone, HttpSession session);

    Result me();

}
