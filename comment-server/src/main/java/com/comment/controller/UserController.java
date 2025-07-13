package com.comment.controller;


import com.comment.dto.LoginFormDTO;
import com.comment.dto.Result;
import com.comment.entity.User;
import com.comment.entity.UserInfo;
import com.comment.service.IUserInfoService;
import com.comment.service.IUserService;
import com.comment.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

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
     * @return Result
     */
    @PostMapping("code")
    public Result<Void> sendCode(@RequestParam("phone") String phone) {
        userService.sendCode(phone);
        return Result.ok();
    }

    /**
     * 用户登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     * @return Result
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginFormDTO loginForm){
        String token = userService.login(loginForm);
        return Result.ok(token);
    }

    /**
     * 用户登出功能
     * @return Result
     */
    @PostMapping("/logout")
    public Result<Void> logout(){
        return Result.fail("功能未完成");
    }

    /**
     * 用户个人信息查询
     *
     * @return User User实体类
     */
    @GetMapping("/me")
    public Result<User> me(){
        // MyBatisPlus查询用户信息
        User user = userService.query().eq("id", UserHolder.getUser().getId()).one();
        return Result.ok(user);
    }

    /**
     * 查看用户信息
     *
     * @param userId 用户id
     * @return Result<UserInfo>
     */
    @GetMapping("/info/{id}")
    public Result<UserInfo> info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        // 设置创建时间
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }
}
