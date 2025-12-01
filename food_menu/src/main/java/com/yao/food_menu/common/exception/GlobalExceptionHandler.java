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
        log.error("数据库约束违反异常: {}", ex.getMessage());
        String message = ex.getMessage();
        
        // 处理唯一约束违反（手机号重复等）
        if (message != null && message.contains("Duplicate entry")) {
            if (message.contains("uk_phone") || message.contains("phone")) {
                return Result.error("手机号已被其他用户使用");
            }
            if (message.contains("uk_username") || message.contains("username")) {
                return Result.error("用户名已被使用");
            }
            // 通用唯一约束错误
            return Result.error("数据已存在，请勿重复添加");
        }
        
        // 处理外键约束违反
        if (message != null && message.contains("foreign key constraint")) {
            return Result.error("操作失败：存在关联数据");
        }
        
        return Result.error("数据操作失败，请检查数据是否正确");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception ex) {
        log.error(ex.getMessage());
        return Result.error(ex.getMessage());
    }
}
