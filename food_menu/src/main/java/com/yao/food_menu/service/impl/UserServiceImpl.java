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

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 模拟验证码存储(生产环境应使用Redis)
    private static final Map<String, String> CODE_CACHE = new HashMap<>();

    @Override
    public void sendCode(String phone) {
        // 模拟发送验证码
        String code = "1234"; // 测试用固定验证码
        CODE_CACHE.put(phone, code);
        log.info("验证码已发送到 {}: {}", phone, code);
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
                // 用户名/密码登录
                String username = loginDto.getUsername();
                if (username == null || username.trim().isEmpty()) {
                    throw new RuntimeException("用户名不能为空");
                }
                username = username.trim();

                log.info("尝试登录，用户名: {}", username);

                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getUsername, username);
                user = this.getOne(queryWrapper);

                if (user == null) {
                    log.warn("用户不存在，用户名: {}", username);
                    // 尝试直接查询数据库，绕过逻辑删除，用于调试
                    try {
                        User debugUser = this.baseMapper.selectOne(queryWrapper);
                        if (debugUser != null) {
                            log.warn("找到用户但可能被逻辑删除过滤，用户ID: {}, deleted值: {}",
                                    debugUser.getId(), debugUser.getDeleted());
                            log.warn("提示：如果deleted字段为NULL，请执行: UPDATE user SET deleted = 0 WHERE deleted IS NULL");
                        }
                    } catch (Exception e) {
                        log.error("调试查询失败: {}", e.getMessage());
                    }
                    throw new RuntimeException("用户不存在");
                }

                log.info("找到用户，ID: {}, 用户名: {}, 状态: {}, deleted: {}",
                        user.getId(), user.getUsername(), user.getStatus(), user.getDeleted());

                if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
                    throw new RuntimeException("密码错误");
                }

            } else if (type == 2) {
                // 手机号/验证码登录
                String cachedCode = CODE_CACHE.get(loginDto.getPhone());
                if (cachedCode == null || !cachedCode.equals(loginDto.getCode())) {
                    throw new RuntimeException("验证码无效");
                }

                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getPhone, loginDto.getPhone());
                user = this.getOne(queryWrapper);

                // 如果用户不存在则自动注册
                if (user == null) {
                    user = new User();
                    user.setPhone(loginDto.getPhone());
                    user.setName("用户_" + loginDto.getPhone().substring(7));
                    user.setStatus(1);
                    this.save(user);
                }

                // 清除已使用的验证码
                CODE_CACHE.remove(loginDto.getPhone());
            } else {
                throw new RuntimeException("无效的登录类型");
            }

            if (user.getStatus() == 0) {
                throw new RuntimeException("用户已被禁用");
            }

            // 生成JWT token（包含familyId和role信息）
            return JwtUtil.generateToken(
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
        return this.getById(userId);
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
        User existingUser = this.getOne(queryWrapper);

        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        BeanUtils.copyProperties(userDto, user);

        // 加密密码
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }

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
        User user = this.getById(userDto.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 获取当前用户角色
        Integer currentUserRole = com.yao.food_menu.common.context.FamilyContext.getUserRole();
        boolean isSuperAdmin = currentUserRole != null && currentUserRole == 2;

        // 更新允许的字段
        if (StringUtils.hasText(userDto.getName())) {
            user.setName(userDto.getName());
        }
        if (StringUtils.hasText(userDto.getPhone())) {
            user.setPhone(userDto.getPhone());
        }
        if (userDto.getStatus() != null) {
            user.setStatus(userDto.getStatus());
        }

        // 只有超级管理员可以修改familyId和role
        if (isSuperAdmin) {
            if (userDto.getFamilyId() != null) {
                user.setFamilyId(userDto.getFamilyId());
            }
            if (userDto.getRole() != null) {
                user.setRole(userDto.getRole());
            }
        }

        this.updateById(user);
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
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        this.updateById(user);
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
}
