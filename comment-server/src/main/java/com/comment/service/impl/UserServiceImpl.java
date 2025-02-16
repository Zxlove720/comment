package com.comment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.comment.constant.UserConstant;
import com.comment.dto.LoginFormDTO;
import com.comment.dto.Result;
import com.comment.dto.UserDTO;
import com.comment.entity.User;
import com.comment.mapper.UserMapper;
import com.comment.service.IUserService;
import com.comment.utils.RegexUtils;
import com.comment.utils.UserHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户相关服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return Result
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
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
     * 用户登录
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     * @return Result
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
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
        User user = query().eq("phone", phone).one();
        // 5.判断用户是否存在
        if (user == null) {
            // 6.用户不存在，那么创建用户
            user = createUserWithPhone(phone);
        }
        // 7.将用户信息转换为UserDTO保存到redis中
        // 7.1 随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 7.2 将User对象转换为HashMap存储
        UserDTO userDTO = new UserDTO();
        BeanUtil.copyProperties(user, userDTO);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 7.3存储
        String tokenKey = UserConstant.USER_LOGIN_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.4设置token有效期
        stringRedisTemplate.expire(tokenKey, UserConstant.USER_LOGIN_TTL, TimeUnit.MINUTES);
        // 8.返回token
        return Result.ok(token);
    }

    /**
     * 新用户注册
     * @param phone 用户电话号码
     * @return User
     */
    public User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        save(user);
        return user;
    }

    @Override
    public Result me() {
        return Result.ok(query().eq("id", UserHolder.getUser().getId()).one());
    }
}
