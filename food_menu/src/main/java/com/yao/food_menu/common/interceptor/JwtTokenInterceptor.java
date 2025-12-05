package com.yao.food_menu.common.interceptor;

import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 * 从Token中提取用户信息并设置到上下文中
 */
@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 从请求头中获取token
        String token = request.getHeader("Authorization");

        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (StringUtils.hasText(token)) {
            try {
                // 验证token
                if (jwtUtil.validateToken(token)) {
                    // 提取用户信息
                    Long userId = jwtUtil.getUserId(token);
                    Long familyId = jwtUtil.getFamilyId(token);
                    Integer role = jwtUtil.getRole(token);

                    // 设置到上下文
                    FamilyContext.setUserId(userId);

                    if (familyId != null) {
                        FamilyContext.setFamilyId(familyId);
                    }

                    if (role != null) {
                        FamilyContext.setUserRole(role);
                    }

                    log.debug("JWT验证成功: userId={}, familyId={}, role={}", userId, familyId, role);
                }
            } catch (Exception e) {
                log.warn("JWT解析失败: {}", e.getMessage());
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 清除上下文
        FamilyContext.clear();
    }
}
