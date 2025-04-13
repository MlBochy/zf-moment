package com.zumo.interceptor;


import com.zumo.utils.JwtUtil;
import com.zumo.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginIntercepter implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoginIntercepter.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        log.info("Received token: {}", token);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            log.info("Parsed claims: {}", claims);
            ThreadLocalUtil.set(claims);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
