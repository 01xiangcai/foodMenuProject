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
 * 小程序端JWT拦截器
 * 专门用于小程序端接口（/uniapp/**），将用户ID设置为WxUserId
 */
@Slf4j
@Component
public class UniappJwtInterceptor implements HandlerInterceptor {

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

                    // 小程序端：设置为WxUserId（与后台管理端区分）
                    FamilyContext.setWxUserId(userId);

                    if (familyId != null) {
                        FamilyContext.setFamilyId(familyId);
                    }

                    if (role != null) {
                        FamilyContext.setUserRole(role);
                    }

                    log.debug("小程序JWT验证成功: wxUserId={}, familyId={}, role={}", userId, familyId, role);
                }
            } catch (Exception e) {
                log.warn("小程序JWT解析失败: {}", e.getMessage());
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
