package com.yao.food_menu.controller;

import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.entity.User;
import com.yao.food_menu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 */
@Tag(name = "用户管理", description = "用户登录、注册、信息管理")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Send verification code
     */
    @Operation(summary = "发送验证码", description = "向指定手机号发送验证码(测试环境固定为1234)")
    @PostMapping("/sendcode")
    public Result<String> sendCode(@RequestParam String phone) {
        log.info("Send code to phone: {}", phone);

        if (phone == null || phone.length() != 11) {
            return Result.error("Invalid phone number");
        }

        userService.sendCode(phone);
        return Result.success("Verification code sent successfully");
    }

    /**
     * User login
     */
    @Operation(summary = "用户登录", description = "支持用户名/密码登录(type=1)和手机号/验证码登录(type=2)")
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDto loginDto) {
        log.info("User login: {}", loginDto);

        try {
            String token = userService.login(loginDto);
            return Result.success(token);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * Get current user info
     */
    @Operation(summary = "获取用户信息", description = "根据Token获取当前登录用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        log.info("Get user info with token: {}", token);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = com.yao.food_menu.common.util.JwtUtil.getUserId(token);
            User user = userService.getCurrentUser(userId);

            // Don't return password
            user.setPassword(null);

            return Result.success(user);
        } catch (Exception e) {
            log.error("Get user info failed: {}", e.getMessage());
            return Result.error("Invalid token");
        }
    }

    /**
     * Update user info
     */
    @Operation(summary = "更新用户信息", description = "更新当前用户的姓名、头像等信息")
    @PutMapping
    public Result<String> update(@RequestBody User user, @RequestHeader("Authorization") String token) {
        log.info("Update user: {}", user);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = com.yao.food_menu.common.util.JwtUtil.getUserId(token);
            user.setId(userId);

            // Don't allow updating password, phone, status
            user.setPassword(null);
            user.setPhone(null);
            user.setStatus(null);

            userService.updateById(user);
            return Result.success("User updated successfully");
        } catch (Exception e) {
            log.error("Update user failed: {}", e.getMessage());
            return Result.error("Update failed");
        }
    }
}
