package com.comment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.comment.constant.ErrorConstant;
import com.comment.constant.MessageConstant;
import com.comment.constant.UserConstant;
import com.comment.dto.LoginFormDTO;
import com.comment.dto.Result;
import com.comment.dto.UserDTO;
import com.comment.entity.User;
import com.comment.mapper.UserMapper;
import com.comment.service.IUserService;
import com.comment.utils.regex.RegexUtils;
import com.comment.utils.UserHolder;
import jakarta.annotation.Resource;
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
     *
     * @param phone 手机号
     */
    @Override
    public void sendCode(String phone) {
        // 1.校验手机号是否合法
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果手机号不合法，返回错误信息
            throw new RuntimeException(ErrorConstant.PHONE_NUMBER_ERROR);
        }
        // 3.手机号合法，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.将验证码保存至Redis
        stringRedisTemplate.opsForValue().set(UserConstant.USER_CODE_KEY + phone, code, 60, TimeUnit.SECONDS);
        // 5.发送验证码（短信功能待完成）
        log.info("发送短信验证码成功，验证码为：{}", code);
        log.info(MessageConstant.CODE_TIME_MESSAGE);
    }

    /**
     * 用户登录
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     * @return String token
     */
    @Override
    public String login(LoginFormDTO loginForm) {
        // 1.校验手机号是否合法
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 1.1如果手机号不合法，则直接返回错误信息
            throw new RuntimeException(ErrorConstant.PHONE_NUMBER_ERROR);
        }
        // 2.校验验证码
        // 2.1获取Redis中存储的验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(UserConstant.USER_CODE_KEY + phone);
        // 2.2获取请求中发送的验证码
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 2.3假如session中没有存储验证码或者验证码比对失败，直接返回
            throw new RuntimeException(ErrorConstant.CODE_ERROR);
        }
        // 3.验证码校验成功，根据手机号查询用户
        User user = query().eq("phone", phone).one();
        // 3.1判断用户是否存在
        if (user == null) {
            // 3.2用户不存在，那么创建用户
            user = createUserWithPhone(phone);
        }
        // 4.将User对象转换为UserDTO对象然后保存到Redis
        // 4.1生成登录用token
        String token = UUID.randomUUID(false).toString();
        // 4.2将User对象转换为UserDTO，然后转换为userMap方便Hash类型操作
        Map<String, Object> userMap = BeanUtil.beanToMap(BeanUtil.copyProperties(user, UserDTO.class), new HashMap<>(),
                // 自定义转换成Map的选项
                CopyOptions
                        .create()
                        // 忽略空值
                        .ignoreNullValue()
                        // 将实体属性值全部转换为String存储
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(UserConstant.USER_LOGIN_KEY + token, userMap);
        // 5.返回token用做登录凭证
        return token;
    }

    /**
     * 新用户注册
     *
     * @param phone 用户电话号码
     * @return User
     */
    public User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("用户_" + RandomUtil.randomString(10));
        save(user);
        return user;
    }

    @Override
    public Result me() {
        return Result.ok(query().eq("id", UserHolder.getUser().getId()).one());
    }
}
