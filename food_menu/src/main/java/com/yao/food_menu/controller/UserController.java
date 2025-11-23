package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.dto.UserDto;
import com.yao.food_menu.dto.UserQueryDto;
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

    /**
     * Page query users
     */
    @Operation(summary = "分页查询用户", description = "支持按用户名、手机号、类型、状态筛选")
    @GetMapping("/page")
    public Result<Page<User>> page(UserQueryDto queryDto) {
        log.info("Page query users: {}", queryDto);

        try {
            Page<User> page = userService.pageUsers(queryDto);
            // Remove passwords from response
            page.getRecords().forEach(user -> user.setPassword(null));
            return Result.success(page);
        } catch (Exception e) {
            log.error("Page query failed: {}", e.getMessage());
            return Result.error("Query failed");
        }
    }

    /**
     * Create user (admin only)
     */
    @Operation(summary = "创建用户", description = "创建管理员用户")
    @PostMapping
    public Result<String> create(@RequestBody UserDto userDto) {
        log.info("Create user: {}", userDto);

        try {
            if (userDto.getUsername() == null || userDto.getPassword() == null) {
                return Result.error("Username and password are required");
            }

            userService.createUser(userDto);
            return Result.success("User created successfully");
        } catch (Exception e) {
            log.error("Create user failed: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * Update user (for admin)
     */
    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    @PutMapping("/admin/update")
    public Result<String> adminUpdate(@RequestBody UserDto userDto) {
        log.info("Admin update user: {}", userDto);

        try {
            if (userDto.getId() == null) {
                return Result.error("User ID is required");
            }

            userService.updateUser(userDto);
            return Result.success("User updated successfully");
        } catch (Exception e) {
            log.error("Update user failed: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * Delete user
     */
    @Operation(summary = "删除用户", description = "软删除用户（设置状态为禁用）")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        log.info("Delete user, id: {}", id);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long currentUserId = com.yao.food_menu.common.util.JwtUtil.getUserId(token);

            // Cannot delete yourself
            if (id.equals(currentUserId)) {
                return Result.error("Cannot delete yourself");
            }

            userService.deleteUser(id);
            return Result.success("User deleted successfully");
        } catch (Exception e) {
            log.error("Delete user failed: {}", e.getMessage());
            return Result.error("Delete failed");
        }
    }

    /**
     * Update user status
     */
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        log.info("Update user status, id: {}, status: {}", id, status);

        try {
            if (status != 0 && status != 1) {
                return Result.error("Invalid status value");
            }

            userService.updateUserStatus(id, status);
            return Result.success("Status updated successfully");
        } catch (Exception e) {
            log.error("Update status failed: {}", e.getMessage());
            return Result.error("Update failed");
        }
    }

    /**
     * Reset user password
     */
    @Operation(summary = "重置用户密码", description = "重置用户密码为随机密码并返回")
    @PutMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable Long id) {
        log.info("Reset password for user: {}", id);

        try {
            String newPassword = userService.resetPassword(id);
            return Result.success(newPassword);
        } catch (Exception e) {
            log.error("Reset password failed: {}", e.getMessage());
            return Result.error("Reset failed");
        }
    }
}
