package com.easychat.config;

import com.easychat.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.contains("/api/user/login") || path.contains("/api/user/register")
                || path.contains("/api/user/info/")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("未登录或Token已过期");
        }
        return true;
    }
}
