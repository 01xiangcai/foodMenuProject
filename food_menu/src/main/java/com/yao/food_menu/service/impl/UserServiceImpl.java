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

    // Simulated verification code storage (in production, use Redis)
    private static final Map<String, String> CODE_CACHE = new HashMap<>();

    @Override
    public void sendCode(String phone) {
        // Simulate sending verification code
        String code = "1234"; // Fixed code for testing
        CODE_CACHE.put(phone, code);
        log.info("Verification code sent to {}: {}", phone, code);
    }

    @Override
    public String login(LoginDto loginDto) {
        User user = null;
        Integer type = resolveLoginType(loginDto);

        if (type == 1) {
            // Username/Password login
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, loginDto.getUsername());
            user = this.getOne(queryWrapper);

            if (user == null) {
                throw new RuntimeException("User not found");
            }

            if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
                throw new RuntimeException("Incorrect password");
            }

        } else if (type == 2) {
            // Phone/Code login
            String cachedCode = CODE_CACHE.get(loginDto.getPhone());
            if (cachedCode == null || !cachedCode.equals(loginDto.getCode())) {
                throw new RuntimeException("Invalid verification code");
            }

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, loginDto.getPhone());
            user = this.getOne(queryWrapper);

            // Auto register if user doesn't exist
            if (user == null) {
                user = new User();
                user.setPhone(loginDto.getPhone());
                user.setName("User_" + loginDto.getPhone().substring(7));
                user.setStatus(1);
                this.save(user);
            }

            // Clear used code
            CODE_CACHE.remove(loginDto.getPhone());
        } else {
            throw new RuntimeException("Invalid login type");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("User is disabled");
        }

        // Generate JWT token
        return JwtUtil.generateToken(user.getId(), user.getUsername() != null ? user.getUsername() : user.getPhone());
    }

    /**
     * Resolve login type from payload, fallback to inferred type when omitted.
     */
    private Integer resolveLoginType(LoginDto loginDto) {
        if (loginDto.getType() != null) {
            return loginDto.getType();
        }

        boolean hasCredential = StringUtils.hasText(loginDto.getUsername())
                && StringUtils.hasText(loginDto.getPassword());
        boolean hasPhoneCode = StringUtils.hasText(loginDto.getPhone()) && StringUtils.hasText(loginDto.getCode());

        if (hasCredential && hasPhoneCode) {
            throw new RuntimeException("Please choose login type explicitly when multiple credentials are provided");
        }

        if (hasCredential) {
            return 1;
        }

        if (hasPhoneCode) {
            return 2;
        }

        throw new RuntimeException("Login type is required");
    }

    @Override
    public User getCurrentUser(Long userId) {
        return this.getById(userId);
    }

    @Override
    public Page<User> pageUsers(UserQueryDto queryDto) {
        Page<User> page = new Page<>(queryDto.getPage(), queryDto.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // Fuzzy search
        queryWrapper.like(StringUtils.hasText(queryDto.getUsername()), User::getUsername, queryDto.getUsername());
        queryWrapper.like(StringUtils.hasText(queryDto.getPhone()), User::getPhone, queryDto.getPhone());
        queryWrapper.like(StringUtils.hasText(queryDto.getName()), User::getName, queryDto.getName());

        // Exact match
        queryWrapper.eq(queryDto.getStatus() != null, User::getStatus, queryDto.getStatus());

        // Order by create time desc
        queryWrapper.orderByDesc(User::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public void createUser(UserDto userDto) {
        // Check if username already exists
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDto.getUsername());
        User existingUser = this.getOne(queryWrapper);

        if (existingUser != null) {
            throw new RuntimeException("Username already exists");
        }

        // Create new user
        User user = new User();
        BeanUtils.copyProperties(userDto, user);

        // Encrypt password
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Set default values
        if (user.getStatus() == null) {
            user.setStatus(1); // Enabled by default
        }

        this.save(user);
    }

    @Override
    public void updateUser(UserDto userDto) {
        User user = this.getById(userDto.getId());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Update allowed fields
        if (StringUtils.hasText(userDto.getName())) {
            user.setName(userDto.getName());
        }
        if (StringUtils.hasText(userDto.getPhone())) {
            user.setPhone(userDto.getPhone());
        }
        if (userDto.getStatus() != null) {
            user.setStatus(userDto.getStatus());
        }

        this.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Soft delete: set status to disabled
        user.setStatus(0);
        this.updateById(user);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    public String resetPassword(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Generate random password (8 characters)
        String newPassword = UUID.randomUUID().toString().substring(0, 8);

        // Encrypt and save
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);

        // Return plain password for admin to notify user
        return newPassword;
    }
}
