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
 * 微信用户控制器
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

    @Autowired
    private com.yao.food_menu.common.config.FileStorageProperties fileStorageProperties;

    /**
     * 发送验证码
     */
    @Operation(summary = "发送验证码", description = "向指定手机号发送验证码(测试环境固定为1234)")
    @PostMapping("/sendcode")
    public Result<String> sendCode(@RequestParam String phone) {
        log.info("发送验证码到手机: {}", phone);

        if (phone == null || phone.length() != 11) {
            return Result.error("无效的手机号码");
        }

        wxUserService.sendCode(phone);
        return Result.success("验证码发送成功");
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "支持用户名/密码登录(type=1)和手机号/验证码登录(type=2)")
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDto loginDto) {
        log.info("微信用户登录: {}", loginDto);

        try {
            String token = wxUserService.login(loginDto);
            return Result.success(token);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "用户名/密码注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody com.yao.food_menu.dto.RegisterDto registerDto) {
        log.info("用户注册: {}", registerDto);

        try {
            if (!StringUtils.hasText(registerDto.getUsername()) || !StringUtils.hasText(registerDto.getPassword())) {
                return Result.error("用户名和密码不能为空");
            }

            wxUserService.register(registerDto);
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 微信一键登录（预留）
     */
    @Operation(summary = "微信一键登录", description = "使用微信授权code登录(预留)")
    @PostMapping("/wxlogin")
    public Result<String> wxLogin(@RequestParam String code) {
        log.info("微信登录,code: {}", code);

        try {
            String token = wxUserService.wxLogin(code);
            return Result.success(token);
        } catch (Exception e) {
            log.error("微信登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @Operation(summary = "上传头像", description = "上传用户头像到OSS专用文件夹")
    @PostMapping("/avatar")
    public Result<UploadResult> uploadAvatar(MultipartFile file, @RequestHeader("Authorization") String token) {
        log.info("用户上传头像");

        try {
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 验证token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = com.yao.food_menu.common.util.JwtUtil.getUserId(token);

            // 获取用户信息并检查限制
            WxUser user = wxUserService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            java.time.LocalDate today = java.time.LocalDate.now();
            // 如果是同一天，检查次数
            if (today.equals(user.getAvatarLastUpdateDate())) {
                if (user.getAvatarUpdateCount() != null && user.getAvatarUpdateCount() >= 3) {
                    return Result.error("今日头像上传次数已达上限(3次)");
                }
            }

            // 上传到存储
            String objectKey = ossService.uploadAvatar(file);
            String presignedUrl = ossService.generatePresignedUrl(objectKey);

            // 更新上传次数和时间
            if (!today.equals(user.getAvatarLastUpdateDate())) {
                user.setAvatarLastUpdateDate(today);
                user.setAvatarUpdateCount(1);
            } else {
                user.setAvatarUpdateCount((user.getAvatarUpdateCount() == null ? 0 : user.getAvatarUpdateCount()) + 1);
            }

            // 如果使用本地存储，需要同时更新localAvatar字段
            if (fileStorageProperties.isLocal() && !objectKey.startsWith("http://")
                    && !objectKey.startsWith("https://")) {
                user.setLocalAvatar(objectKey);
                user.setAvatar(objectKey); // 也更新avatar字段以保持兼容
                log.debug("设置本地头像路径: {}", objectKey);
            }

            // 保存用户信息变更
            wxUserService.updateById(user);

            UploadResult result = new UploadResult(objectKey, presignedUrl);
            return Result.success(result);
        } catch (Exception e) {
            log.error("上传头像失败: {}", e.getMessage());
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @Autowired
    private com.yao.food_menu.common.config.LocalStorageProperties localStorageProperties;

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据Token获取当前登录用户信息")
    @GetMapping("/info")
    public Result<WxUser> getUserInfo(@RequestHeader("Authorization") String token) {
        log.info("获取用户信息,token: {}", token);

        try {
            // 移除"Bearer "前缀(如果存在)
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = com.yao.food_menu.common.util.JwtUtil.getUserId(token);
            WxUser user = wxUserService.getCurrentUser(userId);

            // 不返回密码
            user.setPassword(null);

            // 1. 优先使用本地头像
            if (StringUtils.hasText(user.getLocalAvatar())) {
                String urlPrefix = localStorageProperties.getUrlPrefix();
                if (!urlPrefix.endsWith("/")) {
                    urlPrefix += "/";
                }
                user.setAvatar(urlPrefix + user.getLocalAvatar());
            }
            // 2. 如果存在头像,将objectKey转换为预签名URL
            else if (StringUtils.hasText(user.getAvatar())) {
                String presignedUrl = ossService.generatePresignedUrl(user.getAvatar());
                user.setAvatar(presignedUrl);
            }

            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return Result.error("无效的token");
        }
    }

    /**
     * 更新当前用户信息
     */
    @Operation(summary = "更新用户信息", description = "用户更新自己的信息(昵称、头像等)")
    @PutMapping
    public Result<String> updateUserInfo(@RequestBody UpdateUserDto updateUserDto,
            @RequestHeader("Authorization") String token) {
        log.info("更新用户信息: {}", updateUserDto);

        try {
            // 移除"Bearer "前缀(如果存在)
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = com.yao.food_menu.common.util.JwtUtil.getUserId(token);

            // 获取当前用户
            WxUser user = wxUserService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 更新允许修改的字段
            if (StringUtils.hasText(updateUserDto.getNickname())) {
                user.setNickname(updateUserDto.getNickname());
            }
            if (StringUtils.hasText(updateUserDto.getAvatar())) {
                user.setAvatar(updateUserDto.getAvatar());
                handleAvatarFields(user);
            }
            if (StringUtils.hasText(updateUserDto.getPhone())) {
                user.setPhone(updateUserDto.getPhone());
            }

            wxUserService.updateById(user);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage());
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    // ... (skip update method)

    /**
     * 分页查询用户(管理员)
     */
    @Operation(summary = "分页查询微信用户", description = "管理员查询微信用户列表")
    @GetMapping("/page")
    public Result<Page<WxUser>> page(WxUserQueryDto queryDto) {
        log.info("分页查询微信用户: {}", queryDto);

        try {
            Page<WxUser> page = wxUserService.pageUsers(queryDto);
            // 移除密码并转换头像URL
            page.getRecords().forEach(user -> {
                user.setPassword(null);

                // 1. 优先使用本地头像
                if (StringUtils.hasText(user.getLocalAvatar())) {
                    String urlPrefix = localStorageProperties.getUrlPrefix();
                    if (!urlPrefix.endsWith("/")) {
                        urlPrefix += "/";
                    }
                    user.setAvatar(urlPrefix + user.getLocalAvatar());
                }
                // 2. 如果存在头像,将objectKey转换为预签名URL
                else if (StringUtils.hasText(user.getAvatar())) {
                    try {
                        log.debug("为头像生成预签名URL: {}", user.getAvatar());
                        String presignedUrl = ossService.generatePresignedUrl(user.getAvatar());
                        user.setAvatar(presignedUrl);
                        log.debug("已生成预签名URL: {}", presignedUrl);
                    } catch (Exception e) {
                        log.error("为头像生成预签名URL失败: {}, 错误: {}",
                                user.getAvatar(), e.getMessage(), e);
                        // 降级方案:构造公共OSS URL
                        try {
                            String publicUrl = constructPublicOssUrl(user.getAvatar());
                            user.setAvatar(publicUrl);
                            log.info("使用公共URL降级方案: {}", publicUrl);
                        } catch (Exception ex) {
                            log.error("构造公共URL失败: {}", ex.getMessage());
                            // 如果全部失败,保留原始头像值
                        }
                    }
                } else {
                    log.debug("用户 {} 没有头像", user.getId());
                }
            });
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询失败: {}", e.getMessage(), e);
            return Result.error("查询失败");
        }
    }

    /**
     * 构造公共OSS URL作为降级方案
     */
    private String constructPublicOssUrl(String objectKey) {
        // 格式: https://bucket-name.endpoint/objectKey
        // 目前硬编码 - 理想情况下应该注入AliyunOssProperties
        return "https://food-menu-yao.oss-cn-shenzhen.aliyuncs.com/" + objectKey;
    }

    /**
     * 更新微信用户(管理员)
     */
    @Operation(summary = "更新微信用户", description = "管理员更新微信用户信息")
    @PutMapping("/admin/update")
    public Result<String> adminUpdate(@RequestBody WxUser wxUser) {
        log.info("管理员更新微信用户: {}", wxUser);

        try {
            if (wxUser.getId() == null) {
                return Result.error("用户ID不能为空");
            }

            handleAvatarFields(wxUser);
            wxUserService.updateWxUser(wxUser);
            return Result.success("用户更新成功");
        } catch (Exception e) {
            log.error("更新微信用户失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 处理头像字段，确保本地存储的头像路径同时保存到localAvatar字段
     */
    private void handleAvatarFields(WxUser wxUser) {
        String avatar = wxUser.getAvatar();
        if (!StringUtils.hasText(avatar)) {
            return;
        }

        // 如果使用本地存储，且avatar字段是相对路径（不是完整URL），则同时保存到localAvatar
        if (fileStorageProperties.isLocal() && !avatar.startsWith("http://") && !avatar.startsWith("https://")) {
            wxUser.setLocalAvatar(avatar);
            log.debug("设置本地头像路径: {}", avatar);
        }
    }

    /**
     * 更新用户状态(管理员)
     */
    @Operation(summary = "更新用户状态", description = "管理员启用或禁用微信用户")
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        log.info("更新微信用户状态, id: {}, status: {}", id, status);

        try {
            if (status != 0 && status != 1) {
                return Result.error("无效的状态值");
            }

            wxUserService.updateUserStatus(id, status);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新状态失败: {}", e.getMessage());
            return Result.error("更新失败");
        }
    }

    /**
     * 删除用户(管理员)
     */
    @Operation(summary = "删除微信用户", description = "管理员删除微信用户（软删除）")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除微信用户, id: {}", id);

        try {
            wxUserService.deleteUser(id);
            return Result.success("用户删除成功");
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            return Result.error("删除失败");
        }
    }

    /**
     * 重置用户密码(管理员)
     */
    @Operation(summary = "重置用户密码", description = "管理员重置微信用户密码为随机密码并返回")
    @PutMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable Long id) {
        log.info("重置微信用户密码: {}", id);

        try {
            String newPassword = wxUserService.resetPassword(id);
            return Result.success(newPassword);
        } catch (Exception e) {
            log.error("重置密码失败: {}", e.getMessage());
            return Result.error("重置失败");
        }
    }
}
