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
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户登录、注册、信息管理")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.yao.food_menu.common.util.JwtUtil jwtUtil;

    /**
     * 发送验证码
     */
    @Operation(summary = "发送验证码", description = "向指定手机号发送验证码(测试环境固定为1234)")
    @com.yao.food_menu.common.annotation.RateLimiter(qps = 1, timeout = 100, message = "验证码发送过于频繁，请稍后再试", limitType = com.yao.food_menu.common.annotation.RateLimiter.LimitType.IP)
    @PostMapping("/sendcode")
    public Result<String> sendCode(@RequestParam String phone) {
        log.info("发送验证码到手机: {}", phone);

        if (phone == null || phone.length() != 11) {
            return Result.error("无效的手机号码");
        }

        userService.sendCode(phone);
        return Result.success("验证码发送成功");
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "支持用户名/密码登录(type=1)和手机号/验证码登录(type=2)")
    @com.yao.food_menu.common.annotation.RateLimiter(qps = 5, timeout = 500, message = "登录请求过于频繁，请稍后再试", limitType = com.yao.food_menu.common.annotation.RateLimiter.LimitType.IP)
    @com.yao.food_menu.common.annotation.OperationLog(operationType = com.yao.food_menu.common.annotation.OperationLog.OperationType.LOGIN, operationModule = "用户", operationDesc = "管理员登录", recordParams = true, recordResult = false)
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDto loginDto) {
        // 日志中不输出密码等敏感信息，只输出登录类型和用户标识
        log.info("用户登录请求: 登录类型={}, 用户名={}, 手机号={}",
                loginDto.getType(), loginDto.getUsername(),
                loginDto.getPhone() != null ? loginDto.getPhone().substring(0, 3) + "****" : null);

        try {
            String token = userService.login(loginDto);
            log.info("用户登录成功: 用户名={}", loginDto.getUsername());
            return Result.success(token);
        } catch (Exception e) {
            log.warn("用户登录失败: 用户名={}, 原因={}", loginDto.getUsername(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据Token获取当前登录用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        // Token是敏感信息，日志中不完整输出
        log.debug("获取用户信息请求");

        try {
            // 移除"Bearer "前缀(如果存在)
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            log.debug("解析Token成功, userId={}", userId);
            User user = userService.getCurrentUser(userId);

            // 不返回密码
            user.setPassword(null);

            log.info("获取用户信息成功: userId={}, username={}", user.getId(), user.getUsername());
            return Result.success(user);
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
            return Result.error("无效的token");
        }
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新当前用户的姓名、头像等信息")
    @com.yao.food_menu.common.annotation.OperationLog(operationType = com.yao.food_menu.common.annotation.OperationLog.OperationType.UPDATE, operationModule = "用户", operationDesc = "更新用户信息")
    @PutMapping
    public Result<String> update(@RequestBody User user, @RequestHeader("Authorization") String token) {
        log.info("更新用户信息: {}", user);

        try {
            // 移除"Bearer "前缀(如果存在)
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);
            user.setId(userId);

            // 不允许更新密码、手机号、状态
            user.setPassword(null);
            user.setPhone(null);
            user.setStatus(null);

            userService.updateById(user);
            return Result.success("用户信息更新成功");
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage());
            return Result.error("更新失败");
        }
    }

    /**
     * 分页查询用户
     */
    @Operation(summary = "分页查询用户", description = "支持按用户名、手机号、类型、状态筛选")
    @GetMapping("/page")
    public Result<Page<User>> page(UserQueryDto queryDto) {
        log.info("分页查询用户: {}", queryDto);

        try {
            Page<User> page = userService.pageUsers(queryDto);
            // 从响应中移除密码
            page.getRecords().forEach(user -> user.setPassword(null));
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询失败: {}", e.getMessage());
            return Result.error("查询失败");
        }
    }

    /**
     * 创建用户(仅管理员)
     */
    @Operation(summary = "创建用户", description = "创建管理员用户")
    @com.yao.food_menu.common.annotation.OperationLog(operationType = com.yao.food_menu.common.annotation.OperationLog.OperationType.INSERT, operationModule = "用户", operationDesc = "创建管理员用户")
    @PostMapping
    public Result<String> create(@RequestBody UserDto userDto) {
        log.info("创建用户: {}", userDto);

        try {
            if (userDto.getUsername() == null || userDto.getPassword() == null) {
                return Result.error("用户名和密码不能为空");
            }

            userService.createUser(userDto);
            return Result.success("用户创建成功");
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户(管理员)
     */
    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    @com.yao.food_menu.common.annotation.OperationLog(operationType = com.yao.food_menu.common.annotation.OperationLog.OperationType.UPDATE, operationModule = "用户", operationDesc = "管理员更新用户信息")
    @PutMapping("/admin/update")
    public Result<String> adminUpdate(@RequestBody UserDto userDto) {
        log.info("管理员更新用户: {}", userDto);

        try {
            if (userDto.getId() == null) {
                return Result.error("用户ID不能为空");
            }

            userService.updateUser(userDto);
            return Result.success("用户更新成功");
        } catch (Exception e) {
            log.error("更新用户失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "软删除用户（设置状态为禁用）")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        log.info("删除用户, id: {}", id);

        try {
            // 移除"Bearer "前缀(如果存在)
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long currentUserId = jwtUtil.getUserId(token);

            // 不能删除自己
            if (id.equals(currentUserId)) {
                return Result.error("不能删除自己");
            }

            userService.deleteUser(id);
            return Result.success("用户删除成功");
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            return Result.error("删除失败");
        }
    }

    /**
     * 更新用户状态
     */
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        log.info("更新用户状态, id: {}, status: {}", id, status);

        try {
            if (status != 0 && status != 1) {
                return Result.error("无效的状态值");
            }

            userService.updateUserStatus(id, status);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新状态失败: {}", e.getMessage());
            return Result.error("更新失败");
        }
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码", description = "重置用户密码为随机密码并返回")
    @PutMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable Long id) {
        log.info("重置用户密码: {}", id);

        try {
            String newPassword = userService.resetPassword(id);
            return Result.success(newPassword);
        } catch (Exception e) {
            log.error("重置密码失败: {}", e.getMessage());
            return Result.error("重置失败");
        }
    }

    /**
     * 管理员修改用户密码
     */
    @Operation(summary = "修改用户密码", description = "管理员修改指定用户的密码")
    @PutMapping("/admin/user/update-password")
    public Result<String> updatePassword(@RequestBody com.yao.food_menu.dto.UpdatePasswordDto dto) {
        try {
            userService.updatePassword(dto.getUserId(), dto.getNewPassword());
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改用户密码失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新个人信息
     */
    @Operation(summary = "更新个人信息", description = "更新当前登录用户的个人信息")
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody com.yao.food_menu.dto.UpdateProfileDto updateProfileDto) {
        try {
            userService.updateProfile(updateProfileDto);
            return Result.success("个人信息更新成功");
        } catch (Exception e) {
            log.error("更新个人信息失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改个人密码
     */
    @Operation(summary = "修改个人密码", description = "修改当前登录用户的密码")
    @PutMapping("/profile/password")
    public Result<String> updateOwnPassword(@RequestBody com.yao.food_menu.dto.UpdateOwnPasswordDto dto) {
        try {
            userService.updateOwnPassword(dto.getOldPassword(), dto.getNewPassword());
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改个人密码失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
