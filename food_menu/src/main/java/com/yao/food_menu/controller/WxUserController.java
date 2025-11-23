package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.dto.UpdateUserDto;
import com.yao.food_menu.dto.UploadResult;
import com.yao.food_menu.dto.WxUserQueryDto;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.service.OssService;
import com.yao.food_menu.service.WxUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * WeChat User Controller
 */
@Tag(name = "微信用户管理", description = "微信小程序用户登录、注册、信息管理")
@RestController
@RequestMapping("/wx/user")
@Slf4j
public class WxUserController {

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private OssService ossService;

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

        wxUserService.sendCode(phone);
        return Result.success("Verification code sent successfully");
    }

    /**
     * User login
     */
    @Operation(summary = "用户登录", description = "支持用户名/密码登录(type=1)和手机号/验证码登录(type=2)")
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDto loginDto) {
        log.info("WeChat user login: {}", loginDto);

        try {
            String token = wxUserService.login(loginDto);
            return Result.success(token);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * User register
     */
    @Operation(summary = "用户注册", description = "用户名/密码注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody com.yao.food_menu.dto.RegisterDto registerDto) {
        log.info("User register: {}", registerDto);

        try {
            if (!StringUtils.hasText(registerDto.getUsername()) || !StringUtils.hasText(registerDto.getPassword())) {
                return Result.error("用户名和密码不能为空");
            }

            wxUserService.register(registerDto);
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("Register failed: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * WeChat login (reserved)
     */
    @Operation(summary = "微信一键登录", description = "使用微信授权code登录(预留)")
    @PostMapping("/wxlogin")
    public Result<String> wxLogin(@RequestParam String code) {
        log.info("WeChat login with code: {}", code);

        try {
            String token = wxUserService.wxLogin(code);
            return Result.success(token);
        } catch (Exception e) {
            log.error("WeChat login failed: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * Upload avatar
     */
    @Operation(summary = "上传头像", description = "上传用户头像到OSS专用文件夹")
    @PostMapping("/avatar")
    public Result<UploadResult> uploadAvatar(MultipartFile file, @RequestHeader("Authorization") String token) {
        log.info("Upload avatar for user");

        try {
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // Verify token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = com.yao.food_menu.common.util.JwtUtil.getUserId(token);

            // Upload to OSS
            String objectKey = ossService.uploadAvatar(file);
            String presignedUrl = ossService.generatePresignedUrl(objectKey);

            UploadResult result = new UploadResult(objectKey, presignedUrl);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Upload avatar failed: {}", e.getMessage());
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * Get current user info
     */
    @Operation(summary = "获取用户信息", description = "根据Token获取当前登录用户信息")
    @GetMapping("/info")
    public Result<WxUser> getUserInfo(@RequestHeader("Authorization") String token) {
        log.info("Get user info with token: {}", token);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = com.yao.food_menu.common.util.JwtUtil.getUserId(token);
            WxUser user = wxUserService.getCurrentUser(userId);

            // Don't return password
            user.setPassword(null);

            // Convert avatar objectKey to presigned URL if exists
            if (StringUtils.hasText(user.getAvatar())) {
                String presignedUrl = ossService.generatePresignedUrl(user.getAvatar());
                user.setAvatar(presignedUrl);
            }

            return Result.success(user);
        } catch (Exception e) {
            log.error("Get user info failed: {}", e.getMessage());
            return Result.error("Invalid token");
        }
    }

    /**
     * Update user info
     */
    @Operation(summary = "更新用户信息", description = "更新当前用户的昵称、头像等信息")
    @PutMapping
    public Result<String> update(@RequestBody UpdateUserDto updateDto, @RequestHeader("Authorization") String token) {
        log.info("Update user: {}", updateDto);

        try {
            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = com.yao.food_menu.common.util.JwtUtil.getUserId(token);

            // Build WxUser entity for update
            WxUser user = new WxUser();
            user.setId(userId);
            user.setNickname(updateDto.getNickname());

            // Extract object key if avatar is a full URL
            String avatarKey = ossService.extractKeyFromUrl(updateDto.getAvatar());
            user.setAvatar(avatarKey);

            user.setGender(updateDto.getGender());

            wxUserService.updateById(user);
            return Result.success("用户信息更新成功");
        } catch (Exception e) {
            log.error("Update user failed: {}", e.getMessage());
            return Result.error("更新失败");
        }
    }

    /**
     * Page query users (for admin)
     */
    @Operation(summary = "分页查询微信用户", description = "管理员查询微信用户列表")
    @GetMapping("/page")
    public Result<Page<WxUser>> page(WxUserQueryDto queryDto) {
        log.info("Page query wx users: {}", queryDto);

        try {
            Page<WxUser> page = wxUserService.pageUsers(queryDto);
            // Remove passwords and convert avatar URLs
            page.getRecords().forEach(user -> {
                user.setPassword(null);
                // Convert avatar objectKey to presigned URL if exists
                if (StringUtils.hasText(user.getAvatar())) {
                    try {
                        log.debug("Generating presigned URL for avatar: {}", user.getAvatar());
                        String presignedUrl = ossService.generatePresignedUrl(user.getAvatar());
                        user.setAvatar(presignedUrl);
                        log.debug("Generated presigned URL: {}", presignedUrl);
                    } catch (Exception e) {
                        log.error("Failed to generate presigned URL for avatar: {}, error: {}",
                                user.getAvatar(), e.getMessage(), e);
                        // Fallback: construct public OSS URL
                        try {
                            String publicUrl = constructPublicOssUrl(user.getAvatar());
                            user.setAvatar(publicUrl);
                            log.info("Using public URL fallback: {}", publicUrl);
                        } catch (Exception ex) {
                            log.error("Failed to construct public URL: {}", ex.getMessage());
                            // Keep the original avatar value if all fails
                        }
                    }
                } else {
                    log.debug("User {} has no avatar", user.getId());
                }
            });
            return Result.success(page);
        } catch (Exception e) {
            log.error("Page query failed: {}", e.getMessage(), e);
            return Result.error("Query failed");
        }
    }

    /**
     * Construct public OSS URL as fallback
     */
    private String constructPublicOssUrl(String objectKey) {
        // Format: https://bucket-name.endpoint/objectKey
        // Hardcoded for now - ideally should inject AliyunOssProperties
        return "https://food-menu-yao.oss-cn-shenzhen.aliyuncs.com/" + objectKey;
    }

    /**
     * Update wx user (for admin)
     */
    @Operation(summary = "更新微信用户", description = "管理员更新微信用户信息")
    @PutMapping("/admin/update")
    public Result<String> adminUpdate(@RequestBody WxUser wxUser) {
        log.info("Admin update wx user: {}", wxUser);

        try {
            if (wxUser.getId() == null) {
                return Result.error("User ID is required");
            }

            wxUserService.updateWxUser(wxUser);
            return Result.success("User updated successfully");
        } catch (Exception e) {
            log.error("Update wx user failed: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * Update user status (for admin)
     */
    @Operation(summary = "更新用户状态", description = "管理员启用或禁用微信用户")
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        log.info("Update wx user status, id: {}, status: {}", id, status);

        try {
            if (status != 0 && status != 1) {
                return Result.error("Invalid status value");
            }

            wxUserService.updateUserStatus(id, status);
            return Result.success("Status updated successfully");
        } catch (Exception e) {
            log.error("Update status failed: {}", e.getMessage());
            return Result.error("Update failed");
        }
    }

    /**
     * Delete user (for admin)
     */
    @Operation(summary = "删除微信用户", description = "管理员删除微信用户（软删除）")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        log.info("Delete wx user, id: {}", id);

        try {
            wxUserService.deleteUser(id);
            return Result.success("User deleted successfully");
        } catch (Exception e) {
            log.error("Delete user failed: {}", e.getMessage());
            return Result.error("Delete failed");
        }
    }

    /**
     * Reset user password (for admin)
     */
    @Operation(summary = "重置用户密码", description = "管理员重置微信用户密码为随机密码并返回")
    @PutMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable Long id) {
        log.info("Reset password for wx user: {}", id);

        try {
            String newPassword = wxUserService.resetPassword(id);
            return Result.success(newPassword);
        } catch (Exception e) {
            log.error("Reset password failed: {}", e.getMessage());
            return Result.error("Reset failed");
        }
    }
}
