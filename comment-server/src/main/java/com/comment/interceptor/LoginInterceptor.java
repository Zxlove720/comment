package com.comment.interceptor;

import com.comment.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 登录拦截器
     * @param request 请求
     * @param response 响应
     * @return boolean
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.判断是否需要拦截（ThreadLocal中是否存在用户）
        if (UserHolder.getUser() == null) {
            // 没有用户，那么需要拦截并设置返回状态码
            response.setStatus(401);
            return false;
        }
        // 有用户，那么可以放行
        return true;
    }
}
