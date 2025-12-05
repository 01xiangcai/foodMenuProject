package com.yao.food_menu.common.aspect;

import com.google.common.util.concurrent.RateLimiter;
import com.yao.food_menu.common.annotation.RateLimiter.LimitType;
import com.yao.food_menu.common.context.FamilyContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面
 * 基于Guava RateLimiter实现
 */
@Slf4j
@Aspect
@Component
public class RateLimiterAspect {

    /**
     * 存储每个限流key对应的RateLimiter
     */
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * 环绕通知，对带有@RateLimiter注解的方法进行限流
     */
    @Around("@annotation(com.yao.food_menu.common.annotation.RateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取注解
        com.yao.food_menu.common.annotation.RateLimiter rateLimiterAnnotation = 
            method.getAnnotation(com.yao.food_menu.common.annotation.RateLimiter.class);
        
        // 构建限流key
        String limitKey = buildLimitKey(rateLimiterAnnotation, method);
        
        // 获取或创建RateLimiter
        RateLimiter rateLimiter = limiters.computeIfAbsent(limitKey, key -> {
            log.info("创建限流器: key={}, qps={}", key, rateLimiterAnnotation.qps());
            return RateLimiter.create(rateLimiterAnnotation.qps());
        });
        
        // 尝试获取令牌
        boolean acquired = rateLimiter.tryAcquire(
            rateLimiterAnnotation.timeout(), 
            rateLimiterAnnotation.timeUnit()
        );
        
        if (!acquired) {
            // 限流触发
            log.warn("限流触发: key={}, qps={}", limitKey, rateLimiterAnnotation.qps());
            throw new RuntimeException(rateLimiterAnnotation.message());
        }
        
        // 放行
        return joinPoint.proceed();
    }

    /**
     * 构建限流key
     */
    private String buildLimitKey(com.yao.food_menu.common.annotation.RateLimiter annotation, Method method) {
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();
        LimitType limitType = annotation.limitType();
        
        switch (limitType) {
            case IP:
                // 按IP限流
                String ip = getClientIp();
                return String.format("rate_limit:%s:ip:%s", methodName, ip);
                
            case USER:
                // 按用户ID限流
                Long userId = FamilyContext.getUserId();
                String userKey = userId != null ? userId.toString() : "anonymous";
                return String.format("rate_limit:%s:user:%s", methodName, userKey);
                
            case GLOBAL:
                // 全局限流
                return String.format("rate_limit:%s:global", methodName);
                
            default:
                return String.format("rate_limit:%s", methodName);
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return "unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}

