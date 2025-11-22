package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 工具Controller - 仅用于开发环境
 */
@Tag(name = "开发工具", description = "开发环境辅助工具")
@RestController
@RequestMapping("/dev")
public class DevToolController {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 生成BCrypt密码哈希
     */
    @Operation(summary = "生成密码哈希", description = "输入明文密码，返回BCrypt哈希值")
    @GetMapping("/hash")
    public Result<String> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        return Result.success(hash);
    }

    /**
     * 验证密码
     */
    @Operation(summary = "验证密码", description = "验证明文密码和哈希值是否匹配")
    @GetMapping("/verify")
    public Result<Boolean> verifyPassword(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        return Result.success(matches);
    }
}
