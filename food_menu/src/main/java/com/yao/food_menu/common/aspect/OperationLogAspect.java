package com.yao.food_menu.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yao.food_menu.common.BaseContext;
import com.yao.food_menu.common.annotation.OperationLog;
import com.yao.food_menu.common.context.FamilyContext;
import com.yao.food_menu.common.util.SensitiveDataConverter;
import com.yao.food_menu.entity.User;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.service.OperationLogService;
import com.yao.food_menu.service.UserService;
import com.yao.food_menu.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 自动拦截带有@OperationLog注解的方法，记录操作日志
 * 
 * @author yao
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private WxUserService wxUserService;

    @Autowired(required = false)
    private com.yao.food_menu.common.util.JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 环绕通知，拦截带有@OperationLog注解的方法
     */
    @Around("@annotation(com.yao.food_menu.common.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解信息
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        // 创建操作日志对象
        com.yao.food_menu.entity.OperationLog log = new com.yao.food_menu.entity.OperationLog();

        // 设置操作类型和模块
        log.setOperationType(operationLog.operationType().name());
        log.setOperationModule(operationLog.operationModule());
        log.setOperationDesc(operationLog.operationDesc());

        // 设置方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        log.setMethodName(className + "." + methodName);

        // 获取请求信息
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setIpAddress(getIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        // 记录请求参数（进行敏感信息脱敏）
        if (operationLog.recordParams()) {
            try {
                Object[] args = joinPoint.getArgs();
                String params = objectMapper.writeValueAsString(args);
                // 脱敏处理
                params = SensitiveDataConverter.desensitize(params);
                // 限制参数长度，避免数据过大
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "... (已截断)";
                }
                log.setRequestParams(params);
            } catch (Exception e) {
                log.setRequestParams("参数序列化失败: " + e.getMessage());
            }
        }

        // 设置操作人信息
        setOperatorInfo(log);

        // 设置家庭ID
        Long familyId = FamilyContext.getFamilyId();
        if (familyId != null) {
            log.setFamilyId(familyId);
        }

        // 执行目标方法
        Object result = null;
        try {
            result = joinPoint.proceed();

            // 记录响应结果
            if (operationLog.recordResult() && result != null) {
                try {
                    String resultStr = objectMapper.writeValueAsString(result);
                    // 脱敏处理
                    resultStr = SensitiveDataConverter.desensitize(resultStr);
                    // 限制结果长度
                    if (resultStr.length() > 2000) {
                        resultStr = resultStr.substring(0, 2000) + "... (已截断)";
                    }
                    log.setResponseResult(resultStr);
                } catch (Exception e) {
                    log.setResponseResult("结果序列化失败: " + e.getMessage());
                }
            }

            // 设置成功状态
            log.setStatus(1);

        } catch (Throwable e) {
            // 设置失败状态和错误信息
            log.setStatus(0);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500) + "...";
            }
            log.setErrorMsg(errorMsg);

            // 继续抛出异常
            throw e;
        } finally {
            // 计算执行时长
            long executionTime = System.currentTimeMillis() - startTime;
            log.setExecutionTime(executionTime);
            log.setCreateTime(LocalDateTime.now());

            // 异步保存日志（避免影响业务性能）
            saveLogAsync(log);
        }

        return result;
    }

    /**
     * 设置操作人信息
     */
    private void setOperatorInfo(com.yao.food_menu.entity.OperationLog log) {
        try {
            // 优先从FamilyContext获取用户ID
            // 小程序端优先获取WxUserId，后台管理端获取UserId
            Long currentId = FamilyContext.getWxUserId();

            // 如果WxUserId为空，尝试获取后台管理员UserId
            if (currentId == null) {
                currentId = FamilyContext.getUserId();
            }

            // 如果FamilyContext中都没有，尝试从BaseContext获取（兼容旧代码）
            if (currentId == null) {
                currentId = BaseContext.getCurrentId();
            }

            // 如果还是获取不到，尝试从请求头解析Token
            if (currentId == null && jwtUtil != null) {
                HttpServletRequest request = getHttpServletRequest();
                if (request != null) {
                    String token = request.getHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                    if (token != null && !token.isEmpty()) {
                        try {
                            if (jwtUtil.validateToken(token)) {
                                currentId = jwtUtil.getUserId(token);
                            }
                        } catch (Exception e) {
                            OperationLogAspect.log.debug("从Token解析用户ID失败: {}", e.getMessage());
                        }
                    }
                }
            }

            if (currentId != null) {
                log.setOperatorId(currentId);

                // 尝试从User表获取用户信息（管理员）
                if (userService != null) {
                    try {
                        User user = userService.getById(currentId);
                        if (user != null) {
                            log.setOperatorName(user.getUsername() != null ? user.getUsername() : user.getName());
                            log.setOperatorType("ADMIN");
                            return;
                        }
                    } catch (Exception e) {
                        OperationLogAspect.log.debug("从User表获取用户信息失败: {}", e.getMessage());
                    }
                }

                // 尝试从WxUser表获取用户信息（小程序用户）
                if (wxUserService != null) {
                    try {
                        WxUser wxUser = wxUserService.getById(currentId);
                        if (wxUser != null) {
                            log.setOperatorName(
                                    wxUser.getNickname() != null ? wxUser.getNickname() : wxUser.getUsername());
                            log.setOperatorType("USER");
                            return;
                        }
                    } catch (Exception e) {
                        OperationLogAspect.log.debug("从WxUser表获取用户信息失败: {}", e.getMessage());
                    }
                }

                // 如果都找不到，设置默认值
                log.setOperatorName("用户_" + currentId);
                log.setOperatorType("USER");
            } else {
                // 未登录用户
                log.setOperatorName("匿名用户");
                log.setOperatorType("ANONYMOUS");
            }
        } catch (Exception e) {
            OperationLogAspect.log.error("获取操作人信息失败", e);
            log.setOperatorName("系统");
            log.setOperatorType("SYSTEM");
        }
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            OperationLogAspect.log.debug("获取HttpServletRequest失败", e);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 异步保存日志（避免影响业务性能）
     */
    private void saveLogAsync(com.yao.food_menu.entity.OperationLog operationLog) {
        try {
            // 使用新线程异步保存，避免阻塞业务
            new Thread(() -> {
                try {
                    operationLogService.save(operationLog);
                } catch (Exception e) {
                    OperationLogAspect.log.error("保存操作日志失败: {}", e.getMessage(), e);
                }
            }).start();
        } catch (Exception e) {
            OperationLogAspect.log.error("异步保存日志失败", e);
        }
    }
}
