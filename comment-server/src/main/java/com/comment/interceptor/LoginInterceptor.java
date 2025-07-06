package com.comment.interceptor;

import com.comment.entity.User;
import com.comment.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
        // 从请求中获取Session
        HttpSession session = request.getSession();
        // 从Session中获取用户信息
        User user = (User)session.getAttribute("user");
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
