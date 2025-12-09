package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.dto.UserDto;
import com.yao.food_menu.dto.UserQueryDto;
import com.yao.food_menu.entity.User;
import com.yao.food_menu.mapper.UserMapper;
import com.yao.food_menu.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @org.springframework.beans.factory.annotation.Autowired
    private JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Autowired
    private com.yao.food_menu.service.FamilyService familyService;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 模拟验证码存储(生产环境应使用Redis)
    private static final Map<String, String> CODE_CACHE = new HashMap<>();

    @Override
    public void sendCode(String phone) {
        // 模拟发送验证码
        String code = "1234"; // 测试用固定验证码
        CODE_CACHE.put(phone, code);
        // 验证码是敏感信息，生产环境日志中不应输出明文
        log.info("验证码已发送到手机号: {}", phone.substring(0, 3) + "****" + phone.substring(7));
        log.debug("验证码内容(仅开发环境): {}", code);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String login(LoginDto loginDto) {
        // 标记当前为登录操作，允许跳过user表的数据隔离（仅限登录时）
        com.yao.food_menu.common.context.FamilyContext.setLoginOperation(true);
        try {
            User user = null;
            Integer type = resolveLoginType(loginDto);

            if (type == 1) {
                // 用户名/密码登录 - 支持用户名或手机号
                String loginInput = loginDto.getUsername();
                if (loginInput == null || loginInput.trim().isEmpty()) {
                    throw new RuntimeException("用户名或手机号不能为空");
                }
                loginInput = loginInput.trim();

                log.info("用户尝试密码登录，输入: {}", loginInput);

                // 判断输入的是手机号还是用户名（手机号是11位数字）
                boolean isPhone = loginInput.matches("^1[3-9]\\d{9}$");

                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                if (isPhone) {
                    // 按手机号查询
                    queryWrapper.eq(User::getPhone, loginInput);
                    log.info("识别为手机号登录");
                } else {
                    // 按用户名查询
                    queryWrapper.eq(User::getUsername, loginInput);
                    log.info("识别为用户名登录");
                }
                user = this.getOne(queryWrapper);

                if (user == null) {
                    log.warn("用户登录失败-用户不存在: {}", loginInput);
                    // 尝试直接查询数据库，绕过逻辑删除，用于调试
                    try {
                        User debugUser = this.baseMapper.selectOne(queryWrapper);
                        if (debugUser != null) {
                            log.warn("找到用户但被逻辑删除过滤，用户ID: {}, deleted值: {}",
                                    debugUser.getId(), debugUser.getDeleted());
                            log.warn("提示：如果deleted字段为NULL，请执行: UPDATE user SET deleted = 0 WHERE deleted IS NULL");
                        }
                    } catch (Exception e) {
                        log.error("调试查询失败: {}", e.getMessage());
                    }
                    throw new RuntimeException("用户不存在");
                }

                log.debug("找到用户，ID: {}, 用户名: {}, 状态: {}",
                        user.getId(), user.getUsername(), user.getStatus());

                if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
                    log.warn("用户登录失败-密码错误: userId={}, username={}", user.getId(), loginInput);
                    throw new RuntimeException("密码错误");
                }

            } else if (type == 2) {
                // 手机号/验证码登录
                log.info("用户尝试验证码登录，手机号: {}",
                        loginDto.getPhone().substring(0, 3) + "****" + loginDto.getPhone().substring(7));

                String cachedCode = CODE_CACHE.get(loginDto.getPhone());
                if (cachedCode == null || !cachedCode.equals(loginDto.getCode())) {
                    log.warn("验证码登录失败-验证码无效: phone={}",
                            loginDto.getPhone().substring(0, 3) + "****" + loginDto.getPhone().substring(7));
                    throw new RuntimeException("验证码无效");
                }

                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getPhone, loginDto.getPhone());
                user = this.getOne(queryWrapper);

                // 如果用户不存在则自动注册
                if (user == null) {
                    log.info("新用户通过验证码登录，自动注册: phone={}",
                            loginDto.getPhone().substring(0, 3) + "****" + loginDto.getPhone().substring(7));
                    user = new User();
                    user.setPhone(loginDto.getPhone());
                    user.setName("用户_" + loginDto.getPhone().substring(7));
                    user.setStatus(1);
                    this.save(user);
                    log.info("新用户注册成功: userId={}", user.getId());
                }

                // 清除已使用的验证码
                CODE_CACHE.remove(loginDto.getPhone());
            } else {
                log.error("无效的登录类型: {}", type);
                throw new RuntimeException("无效的登录类型");
            }

            if (user.getStatus() == 0) {
                log.warn("用户已被禁用: userId={}, username={}", user.getId(), user.getUsername());
                throw new RuntimeException("用户已被禁用");
            }

            // 生成JWT token（包含familyId和role信息）
            log.info("用户登录成功: userId={}, username={}, familyId={}, role={}",
                    user.getId(), user.getUsername() != null ? user.getUsername() : "手机用户",
                    user.getFamilyId(), user.getRole());

            return jwtUtil.generateToken(
                    user.getId(),
                    user.getUsername() != null ? user.getUsername() : user.getPhone(),
                    user.getFamilyId(),
                    user.getRole());
        } finally {
            // 清除登录操作标记
            com.yao.food_menu.common.context.FamilyContext.setLoginOperation(false);
        }
    }

    /**
     * 从请求中解析登录类型,当未指定时回退到推断类型
     */
    private Integer resolveLoginType(LoginDto loginDto) {
        if (loginDto.getType() != null) {
            return loginDto.getType();
        }

        boolean hasCredential = StringUtils.hasText(loginDto.getUsername())
                && StringUtils.hasText(loginDto.getPassword());
        boolean hasPhoneCode = StringUtils.hasText(loginDto.getPhone()) && StringUtils.hasText(loginDto.getCode());

        if (hasCredential && hasPhoneCode) {
            throw new RuntimeException("提供多个凭证时请明确指定登录类型");
        }

        if (hasCredential) {
            return 1;
        }

        if (hasPhoneCode) {
            return 2;
        }

        throw new RuntimeException("登录类型不能为空");
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public User getCurrentUser(Long userId) {
        User user = this.getById(userId);
        if (user != null && user.getFamilyId() != null) {
            com.yao.food_menu.entity.Family family = familyService.getById(user.getFamilyId());
            if (family != null) {
                user.setFamilyName(family.getName());
            }
        }
        return user;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<User> pageUsers(UserQueryDto queryDto) {
        Page<User> page = new Page<>(queryDto.getPage(), queryDto.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // Fuzzy search
        queryWrapper.like(StringUtils.hasText(queryDto.getUsername()), User::getUsername, queryDto.getUsername());
        queryWrapper.like(StringUtils.hasText(queryDto.getPhone()), User::getPhone, queryDto.getPhone());
        queryWrapper.like(StringUtils.hasText(queryDto.getName()), User::getName, queryDto.getName());

        // Exact match
        queryWrapper.eq(queryDto.getStatus() != null, User::getStatus, queryDto.getStatus());

        // 超级管理员可以通过familyId参数筛选特定家庭的数据
        // 非超级管理员由拦截器自动过滤，这里不需要处理
        if (queryDto.getFamilyId() != null && com.yao.food_menu.common.context.FamilyContext.isSuperAdmin()) {
            queryWrapper.eq(User::getFamilyId, queryDto.getFamilyId());
        }

        // Order by create time desc
        queryWrapper.orderByDesc(User::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void createUser(UserDto userDto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDto.getUsername());
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setName(userDto.getName());
        // 手机号可选,如果为空则设置为空字符串(数据库字段可能不允许null)
        user.setPhone(StringUtils.hasText(userDto.getPhone()) ? userDto.getPhone() : "");
        user.setAvatar(userDto.getAvatar());
        user.setStatus(userDto.getStatus() != null ? userDto.getStatus() : 1);

        // 处理家庭ID和角色
        // 获取当前用户角色
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        boolean isSuperAdmin = currentUserRole != null && currentUserRole == 2;

        if (isSuperAdmin) {
            // 超级管理员可以设置familyId和role
            if (userDto.getFamilyId() != null) {
                user.setFamilyId(userDto.getFamilyId());
            }
            if (userDto.getRole() != null) {
                user.setRole(userDto.getRole());
            } else {
                user.setRole(0); // 默认为普通管理员
            }
        } else {
            // 非超级管理员创建的用户自动使用创建者的familyId
            Long currentFamilyId = com.yao.food_menu.common.context.FamilyContext.getFamilyId();
            user.setFamilyId(currentFamilyId);
            user.setRole(0); // 默认为普通管理员
        }

        this.save(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateUser(UserDto userDto) {
        User existingUser = this.getById(userDto.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 获取当前操作者的角色
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        boolean isSuperAdmin = currentUserRole != null && currentUserRole == 2;
        boolean isFamilyAdmin = currentUserRole != null && currentUserRole == 1;

        // 用户名修改权限验证
        if (userDto.getUsername() != null && !userDto.getUsername().equals(existingUser.getUsername())) {
            // 只有超级管理员和家庭管理员可以修改用户名
            if (!isSuperAdmin && !isFamilyAdmin) {
                throw new RuntimeException("权限不足,只有超级管理员和家庭管理员可以修改用户名");
            }

            // 检查新用户名是否已被使用
            LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
            usernameWrapper.eq(User::getUsername, userDto.getUsername());
            usernameWrapper.ne(User::getId, userDto.getId());
            if (this.count(usernameWrapper) > 0) {
                throw new RuntimeException("用户名已被使用");
            }

            existingUser.setUsername(userDto.getUsername());
        }

        // 手机号唯一性验证
        if (StringUtils.hasText(userDto.getPhone()) && !userDto.getPhone().equals(existingUser.getPhone())) {
            // 检查新手机号是否已被其他用户使用
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, userDto.getPhone());
            phoneWrapper.ne(User::getId, userDto.getId());
            if (this.count(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已被使用");
            }
            existingUser.setPhone(userDto.getPhone());
        }

        // 更新其他允许的字段
        if (StringUtils.hasText(userDto.getName())) {
            existingUser.setName(userDto.getName());
        }
        if (StringUtils.hasText(userDto.getAvatar())) {
            existingUser.setAvatar(userDto.getAvatar());
        }
        if (userDto.getRole() != null) {
            existingUser.setRole(userDto.getRole());
        }
        if (userDto.getStatus() != null) {
            existingUser.setStatus(userDto.getStatus());
        }
        if (userDto.getFamilyId() != null) {
            existingUser.setFamilyId(userDto.getFamilyId());
        }

        this.updateById(existingUser);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteUser(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 软删除:设置状态为禁用
        user.setStatus(0);
        this.updateById(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateUserStatus(Long id, Integer status) {
        // 获取当前操作者的角色和ID
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        Long currentUserId = com.yao.food_menu.common.context.FamilyContext.getUserId();

        // 不能禁用自己
        if (id.equals(currentUserId)) {
            throw new RuntimeException("不能禁用自己");
        }

        // 获取目标用户
        User targetUser = this.getById(id);
        if (targetUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 权限验证: 只有角色权限高的可以禁用角色权限低的
        // 角色等级: 超级管理员(2) > 家庭管理员(1) > 普通管理员(0)
        Integer targetUserRole = targetUser.getRole() != null ? targetUser.getRole() : 0;
        Integer myRole = currentUserRole != null ? currentUserRole : 0;

        if (myRole <= targetUserRole) {
            throw new RuntimeException("权限不足,只能禁用角色权限低于您的用户");
        }

        targetUser.setStatus(status);
        this.updateById(targetUser);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String resetPassword(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 生成8位随机密码
        String newPassword = UUID.randomUUID().toString().substring(0, 8);

        // 加密并保存
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);

        // 返回明文密码供管理员通知用户
        return newPassword;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updatePassword(Long userId, String newPassword) {
        // 获取当前操作者的角色、ID和家庭ID
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        Long currentUserId = com.yao.food_menu.common.context.FamilyContext.getUserId();
        Long currentUserFamilyId = com.yao.food_menu.common.context.FamilyContext.getFamilyId();

        // 不能修改自己的密码(应该使用修改个人密码功能)
        if (userId.equals(currentUserId)) {
            throw new RuntimeException("不能通过此功能修改自己的密码,请使用修改个人密码功能");
        }

        // 获取目标用户
        User targetUser = this.getById(userId);
        if (targetUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 权限验证
        boolean isSuperAdmin = currentUserRole != null && currentUserRole == 2;
        boolean isFamilyAdmin = currentUserRole != null && currentUserRole == 1;

        // 普通管理员(role=0)没有修改密码的权限
        if (!isSuperAdmin && !isFamilyAdmin) {
            throw new RuntimeException("权限不足,您没有修改用户密码的权限");
        }

        // 家庭管理员的权限限制
        if (isFamilyAdmin && !isSuperAdmin) {
            // 只能修改本家庭的用户
            if (currentUserFamilyId == null || !currentUserFamilyId.equals(targetUser.getFamilyId())) {
                throw new RuntimeException("权限不足,只能修改本家庭用户的密码");
            }

            // 只能修改普通管理员(role=0)的密码,不能修改同级或更高级别的管理员
            Integer targetUserRole = targetUser.getRole() != null ? targetUser.getRole() : 0;
            if (targetUserRole >= 1) {
                throw new RuntimeException("权限不足,只能修改普通管理员的密码");
            }
        }

        // 验证新密码
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("密码长度至少6位");
        }

        // 加密并更新密码
        targetUser.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(targetUser);

        log.info("管理员修改用户密码成功: operatorRole={}, targetUserId={}", currentUserRole, userId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateProfile(com.yao.food_menu.dto.UpdateProfileDto updateProfileDto) {
        // 获取当前用户ID
        Long currentUserId = com.yao.food_menu.common.context.FamilyContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("未登录");
        }

        // 获取当前用户
        User currentUser = this.getById(currentUserId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新姓名
        if (StringUtils.hasText(updateProfileDto.getName())) {
            currentUser.setName(updateProfileDto.getName());
        }

        // 更新手机号(需要检查唯一性)
        if (StringUtils.hasText(updateProfileDto.getPhone())
                && !updateProfileDto.getPhone().equals(currentUser.getPhone())) {
            // 检查新手机号是否已被其他用户使用
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, updateProfileDto.getPhone());
            phoneWrapper.ne(User::getId, currentUserId);
            if (this.count(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已被使用");
            }
            currentUser.setPhone(updateProfileDto.getPhone());
        }

        // 更新头像
        if (StringUtils.hasText(updateProfileDto.getAvatar())) {
            currentUser.setAvatar(updateProfileDto.getAvatar());
        }

        this.updateById(currentUser);
        log.info("用户更新个人信息成功: userId={}", currentUserId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateOwnPassword(String oldPassword, String newPassword) {
        // 获取当前用户ID
        Long currentUserId = com.yao.food_menu.common.context.FamilyContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("未登录");
        }

        // 获取当前用户
        User currentUser = this.getById(currentUserId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 验证新密码
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("密码长度至少6位");
        }

        // 加密并更新密码
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(currentUser);

        log.info("用户修改个人密码成功: userId={}", currentUserId);
    }
}
