package com.yao.food_menu.common.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yao.food_menu.common.annotation.PreventDuplicateSubmit;
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
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交切面
 * 基于Caffeine本地缓存实现
 */
@Slf4j
@Aspect
@Component
public class PreventDuplicateSubmitAspect {

    /**
     * 缓存，存储已提交的请求key
     * key: 请求标识，value: 提交时间戳
     */
    private final Cache<String, Long> submitCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS) // 1小时后自动过期
            .maximumSize(10000) // 最大缓存10000个key
            .build();

    /**
     * 环绕通知，对带有@PreventDuplicateSubmit注解的方法进行防重复提交检查
     */
    @Around("@annotation(com.yao.food_menu.common.annotation.PreventDuplicateSubmit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        PreventDuplicateSubmit annotation = method.getAnnotation(PreventDuplicateSubmit.class);

        // 构建请求唯一标识
        String submitKey = buildSubmitKey(annotation, method);

        // 获取上次提交时间
        Long lastSubmitTime = submitCache.getIfPresent(submitKey);
        long currentTime = System.currentTimeMillis();

        if (lastSubmitTime != null) {
            long timeDiff = currentTime - lastSubmitTime;
            if (timeDiff < annotation.interval()) {
                // 在时间间隔内重复提交
                long remainingTime = (annotation.interval() - timeDiff) / 1000;
                String message = String.format("%s，请%d秒后再试",
                        annotation.message(), remainingTime);
                log.warn("重复提交拦截: key={}, interval={}ms", submitKey, annotation.interval());
                throw new RuntimeException(message);
            }
        }

        // 记录本次提交时间
        submitCache.put(submitKey, currentTime);

        try {
            // 执行业务方法
            return joinPoint.proceed();
        } catch (Exception e) {
            // 如果业务执行失败，移除缓存，允许重试
            submitCache.invalidate(submitKey);
            throw e;
        }
    }

    /**
     * 构建提交唯一标识
     */
    private String buildSubmitKey(PreventDuplicateSubmit annotation, Method method) {
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();

        if (annotation.byUser()) {
            // 基于用户维度（优先获取小程序用户ID，其次后台管理员ID）
            Long userId = FamilyContext.getWxUserId();
            if (userId == null) {
                userId = FamilyContext.getUserId();
            }
            String userKey = userId != null ? userId.toString() : "anonymous";
            return String.format("duplicate_submit:%s:user:%s", methodName, userKey);
        } else {
            // 基于IP维度
            String ip = getClientIp();
            return String.format("duplicate_submit:%s:ip:%s", methodName, ip);
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

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
