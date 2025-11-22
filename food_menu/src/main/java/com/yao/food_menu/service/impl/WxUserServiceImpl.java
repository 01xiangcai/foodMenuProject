package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.mapper.WxUserMapper;
import com.yao.food_menu.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

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
}
