package com.comment.controller;


import cn.hutool.core.util.RandomUtil;
import com.comment.dto.LoginFormDTO;
import com.comment.dto.Result;
import com.comment.entity.User;
import com.comment.entity.UserInfo;
import com.comment.service.IUserInfoService;
import com.comment.service.IUserService;
import com.comment.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

/**
 * <p>
 * 前端控制器
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
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // 1.校验手机号是否合法
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果手机号不合法，返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 3.手机号合法，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.将验证码保存至 session
        session.setAttribute("code", code);
        // 5.发送验证码（短信功能待完成）
        log.info("发送短信验证码成功，验证码为：{}", code);
        // 响应结果
        return Result.ok();
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // 1.校验手机号是否合法
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果手机号不合法，则直接返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 3.校验验证码
            // 获取session中存储的验证码
        Object cacheCode = session.getAttribute("code");
            // 获取请求中发送的验证码
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
            // 假如session中没有存储验证码或者验证码比对失败，直接返回
            return Result.fail("验证码错误");
        }
        // 4.验证码校验成功，根据手机号查询用户 Mybatis-plus
        User user = userService.query().eq("phone", phone).one();
        // 5.判断用户是否存在
        if (user == null) {
            // 6.用户不存在，那么创建用户
        }


        return Result.fail("功能未完成");
    }

    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(){
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    @GetMapping("/me")
    public Result me(){
        // TODO 获取当前登录的用户并返回
        return Result.fail("功能未完成");
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
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
