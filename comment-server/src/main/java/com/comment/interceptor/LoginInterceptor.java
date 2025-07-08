package com.comment.interceptor;

import cn.hutool.core.util.StrUtil;
import com.comment.entity.User;
import com.comment.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 登录拦截器
     * @param request 请求
     * @param response 响应
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.从请求头中获取token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            // 1.1token不存在，拦截并返回401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 2.有token，从Redis中获取用户信息

        // 判断是否有用户信息
        if (user == null) {
            // 如果没有用户信息，则返回未登录
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        // 如果有用户信息，则将用户信息保存到ThreadLocal
        UserHolder.saveUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 当请求完成后，从ThreadLocal中删除用户信息
        UserHolder.removeUser();
    }
}
