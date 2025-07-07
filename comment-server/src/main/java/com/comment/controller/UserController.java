package com.comment.controller;


import com.comment.dto.LoginFormDTO;
import com.comment.dto.Result;
import com.comment.entity.UserInfo;
import com.comment.service.IUserInfoService;
import com.comment.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

/**
 * <p>
 * 用户相关controller
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码
     *
     * @param phone   手机号
     * @param session
     * @return Result
     */
    @PostMapping("code")
    public Result<Void> sendCode(@RequestParam("phone") String phone, HttpSession session) {
        userService.sendCode(phone, session);
        return Result.ok();
    }

    /**
     * 用户登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     * @return Result
     */
    @PostMapping("/login")
    public Result<Void> login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        userService.login(loginForm, session);
        return Result.ok();
    }

    /**
     * 用户登出功能
     * @return Result
     */
    @PostMapping("/logout")
    public Result<Void> logout(){
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    @GetMapping("/me")
    public Result<Void> me(){
       return userService.me();
    }

    @GetMapping("/info/{id}")
    public Result<UserInfo> info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }
}
