package com.yao.food_menu.common.exception;

import com.yao.food_menu.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = { RestController.class, Controller.class })
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        // 记录详细的异常信息和堆栈，用于排查问题
        log.error("数据库约束违反异常: {}", ex.getMessage(), ex);
        String message = ex.getMessage();
        
        // 处理唯一约束违反（手机号重复等）
        if (message != null && message.contains("Duplicate entry")) {
            if (message.contains("uk_phone") || message.contains("phone")) {
                log.warn("手机号重复注册尝试");
                return Result.error("手机号已被其他用户使用");
            }
            if (message.contains("uk_username") || message.contains("username")) {
                log.warn("用户名重复注册尝试");
                return Result.error("用户名已被使用");
            }
            // 通用唯一约束错误
            log.warn("数据唯一约束冲突: {}", message);
            return Result.error("数据已存在，请勿重复添加");
        }
        
        // 处理外键约束违反
        if (message != null && message.contains("foreign key constraint")) {
            log.warn("外键约束违反: {}", message);
            return Result.error("操作失败：存在关联数据");
        }
        
        return Result.error("数据操作失败，请检查数据是否正确");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception ex) {
        // 记录未处理的异常，包含完整的堆栈信息
        log.error("全局异常捕获: {}", ex.getMessage(), ex);
        
        // 避免敏感信息泄露，生产环境返回通用错误消息
        String errorMessage = ex.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "系统异常，请稍后重试";
        }
        
        return Result.error(errorMessage);
    }
}
