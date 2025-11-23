package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.dto.WxUserQueryDto;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.mapper.WxUserMapper;
import com.yao.food_menu.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements WxUserService {

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
        WxUser user = null;
        Integer type = resolveLoginType(loginDto);

        if (type == 1) {
            // Username/Password login
            LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WxUser::getUsername, loginDto.getUsername());
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

            LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WxUser::getPhone, loginDto.getPhone());
            user = this.getOne(queryWrapper);

            // Auto register if user doesn't exist
            if (user == null) {
                user = new WxUser();
                user.setPhone(loginDto.getPhone());
                user.setNickname("用户_" + loginDto.getPhone().substring(7));
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
    public WxUser getCurrentUser(Long userId) {
        return this.getById(userId);
    }

    @Override
    public void register(com.yao.food_menu.dto.RegisterDto registerDto) {
        // Check if username exists
        LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WxUser::getUsername, registerDto.getUsername());
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // Check if phone exists (if provided)
        if (StringUtils.hasText(registerDto.getPhone())) {
            LambdaQueryWrapper<WxUser> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(WxUser::getPhone, registerDto.getPhone());
            if (this.count(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已注册");
            }
        }

        // Create new user
        WxUser user = new WxUser();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setNickname(StringUtils.hasText(registerDto.getNickname()) ? registerDto.getNickname()
                : "用户_" + registerDto.getUsername());
        user.setPhone(registerDto.getPhone());
        user.setStatus(1); // Normal status
        user.setGender(0); // Unknown gender

        this.save(user);
    }

    @Override
    public String wxLogin(String code) {
        // TODO: Implement WeChat login
        // 1. Call WeChat API to get openid using code
        // 2. Find or create user by openid
        // 3. Generate and return JWT token
        throw new RuntimeException("WeChat login not implemented yet");
    }

    @Override
    public Page<WxUser> pageUsers(WxUserQueryDto queryDto) {
        Page<WxUser> page = new Page<>(queryDto.getPage(), queryDto.getPageSize());
        LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();

        // Fuzzy search by nickname
        if (StringUtils.hasText(queryDto.getNickname())) {
            queryWrapper.like(WxUser::getNickname, queryDto.getNickname());
        }

        // Fuzzy search by phone
        if (StringUtils.hasText(queryDto.getPhone())) {
            queryWrapper.like(WxUser::getPhone, queryDto.getPhone());
        }

        // Fuzzy search by username
        if (StringUtils.hasText(queryDto.getUsername())) {
            queryWrapper.like(WxUser::getUsername, queryDto.getUsername());
        }

        // Filter by status
        if (queryDto.getStatus() != null) {
            queryWrapper.eq(WxUser::getStatus, queryDto.getStatus());
        }

        // Order by create time descending
        queryWrapper.orderByDesc(WxUser::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public void updateWxUser(WxUser wxUser) {
        WxUser existingUser = this.getById(wxUser.getId());
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }

        // Update allowed fields
        if (StringUtils.hasText(wxUser.getNickname())) {
            existingUser.setNickname(wxUser.getNickname());
        }
        if (StringUtils.hasText(wxUser.getPhone())) {
            existingUser.setPhone(wxUser.getPhone());
        }
        if (wxUser.getGender() != null) {
            existingUser.setGender(wxUser.getGender());
        }
        if (wxUser.getStatus() != null) {
            existingUser.setStatus(wxUser.getStatus());
        }

        this.updateById(existingUser);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        WxUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        WxUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Soft delete by setting status to 0
        user.setStatus(0);
        this.updateById(user);
    }

    @Override
    public String resetPassword(Long id) {
        WxUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Generate random 8-character password
        String newPassword = generateRandomPassword(8);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);

        return newPassword;
    }

    /**
     * Generate random password
     */
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
