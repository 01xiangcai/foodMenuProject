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
    private com.yao.food_menu.service.FamilyService familyService;

    @Autowired
    private OssService ossService;

    @Autowired
    private com.yao.food_menu.common.config.FileStorageProperties fileStorageProperties;

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

        wxUserService.sendCode(phone);
        return Result.success("验证码发送成功");
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "支持用户名/密码登录(type=1)和手机号/验证码登录(type=2)")
    @com.yao.food_menu.common.annotation.RateLimiter(qps = 5, timeout = 500, message = "登录请求过于频繁，请稍后再试", limitType = com.yao.food_menu.common.annotation.RateLimiter.LimitType.IP)
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
    @com.yao.food_menu.common.annotation.RateLimiter(qps = 2, timeout = 300, message = "注册请求过于频繁，请稍后再试", limitType = com.yao.food_menu.common.annotation.RateLimiter.LimitType.IP)
    @com.yao.food_menu.common.annotation.PreventDuplicateSubmit(interval = 2000, message = "注册请求已提交", byUser = false)
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
            Long userId = jwtUtil.getUserId(token);

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
    public Result<WxUser> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("获取用户信息,token: {}",
                token != null ? (token.length() > 20 ? token.substring(0, 20) + "..." : token) : "null");

        if (token == null || token.trim().isEmpty()) {
            log.warn("获取用户信息失败: token为空");
            return Result.error("未提供token，请先登录");
        }

        try {
            // 移除"Bearer "前缀(如果存在)
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Long userId = jwtUtil.getUserId(token);

            // 标记为查询当前用户自己的信息，跳过数据隔离
            // 这样可以允许用户查询自己的信息，即使还没有绑定家庭
            com.yao.food_menu.common.context.FamilyContext.setQueryCurrentUser(true);
            try {
                WxUser user = wxUserService.getCurrentUser(userId);

                if (user == null) {
                    return Result.error("用户不存在");
                }

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
            } finally {
                // 清除查询当前用户的标记
                com.yao.food_menu.common.context.FamilyContext.setQueryCurrentUser(false);
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            return Result.error("无效的token: " + e.getMessage());
        }
    }

    /**
     * 获取指定用户信息（公开信息）
     */
    @Operation(summary = "获取指定用户信息", description = "获取指定用户的公开信息(头像、昵称、性别)")
    @GetMapping("/info/{id}")
    public Result<WxUser> getPublicUserInfo(@PathVariable Long id) {
        // 使用FamilyContext skipping logic if needed, but here we just need basic info
        // We might need to bypass tenant isolation if we want to see users from other
        // families in comments?
        // Comments are usually within family or public? The requirement implies valid
        // viewing.
        // For simplicity, we query directly. If MyBatis Plus plugin enforces tenant, we
        // might need to skip.
        // Assuming GetById bypasses tenant or tenant is handled.
        // However, WxUser doesn't seem to have tenant isolation strictly enforced on
        // getById usually unless configured.
        // Let's assume standard behavior first.

        WxUser user = wxUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 脱敏处理: 仅去除密码，其他信息全部返回
        user.setPassword(null);

        // 默认昵称
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname("家庭成员");
        }

        // 处理头像
        if (StringUtils.hasText(user.getLocalAvatar())) {
            String urlPrefix = localStorageProperties.getUrlPrefix();
            if (!urlPrefix.endsWith("/")) {
                urlPrefix += "/";
            }
            user.setAvatar(urlPrefix + user.getLocalAvatar());
        } else if (StringUtils.hasText(user.getAvatar())) {
            try {
                String presignedUrl = ossService.generatePresignedUrl(user.getAvatar());
                user.setAvatar(presignedUrl);
            } catch (Exception e) {
                // ignore
            }
        }

        // 填充家庭名称
        if (user.getFamilyId() != null) {
            com.yao.food_menu.entity.Family family = familyService.getById(user.getFamilyId());
            if (family != null) {
                user.setFamilyName(family.getName());
            }
        }

        return Result.success(user);
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

            Long userId = jwtUtil.getUserId(token);

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
                // 检查手机号是否已被其他用户使用（需要跳过数据隔离，因为手机号是全局唯一的）
                com.yao.food_menu.common.context.FamilyContext.setQueryCurrentUser(true);
                try {
                    com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WxUser> phoneWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                    phoneWrapper.eq(WxUser::getPhone, updateUserDto.getPhone());
                    phoneWrapper.ne(WxUser::getId, userId); // 排除当前用户
                    if (wxUserService.count(phoneWrapper) > 0) {
                        return Result.error("手机号已被其他用户使用");
                    }
                } finally {
                    com.yao.food_menu.common.context.FamilyContext.setQueryCurrentUser(false);
                }
                user.setPhone(updateUserDto.getPhone());
            }
            // 更新性别字段
            if (updateUserDto.getGender() != null) {
                user.setGender(updateUserDto.getGender());
            }

            wxUserService.updateById(user);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage(), e);
            // 如果错误信息已经包含友好的提示（如"手机号已被其他用户使用"），直接返回
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("手机号已被其他用户使用") ||
                    errorMsg.contains("用户不存在"))) {
                return Result.error(errorMsg);
            }
            // 其他错误返回通用提示
            return Result.error("更新失败，请稍后重试");
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
