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
    public String login(LoginDto loginDto) {
        WxUser user = null;
        Integer type = resolveLoginType(loginDto);

        if (type == 1) {
            // 用户名/密码登录
            LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WxUser::getUsername, loginDto.getUsername());
            user = this.getOne(queryWrapper);

            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
                throw new RuntimeException("密码错误");
            }

        } else if (type == 2) {
            // 手机号/验证码登录
            String cachedCode = CODE_CACHE.get(loginDto.getPhone());
            if (cachedCode == null || !cachedCode.equals(loginDto.getCode())) {
                throw new RuntimeException("验证码无效");
            }

            LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WxUser::getPhone, loginDto.getPhone());
            user = this.getOne(queryWrapper);

            // 如果用户不存在则自动注册
            if (user == null) {
                user = new WxUser();
                user.setPhone(loginDto.getPhone());
                user.setNickname("用户_" + loginDto.getPhone().substring(7));
                // 默认角色为普通用户
                user.setRole(0);
                this.save(user);
            }

            // 清除已使用的验证码
            CODE_CACHE.remove(loginDto.getPhone());
        } else {
            throw new RuntimeException("无效的登录类型");
        }

        // 生成JWT token
        return JwtUtil.generateToken(user.getId(), user.getUsername() != null ? user.getUsername() : user.getPhone());
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
    public WxUser getCurrentUser(Long userId) {
        return this.getById(userId);
    }

    @Override
    public void register(com.yao.food_menu.dto.RegisterDto registerDto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WxUser::getUsername, registerDto.getUsername());
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在(如果提供)
        if (StringUtils.hasText(registerDto.getPhone())) {
            LambdaQueryWrapper<WxUser> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(WxUser::getPhone, registerDto.getPhone());
            if (this.count(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已注册");
            }
        }

        // 创建新用户
        WxUser user = new WxUser();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setNickname(StringUtils.hasText(registerDto.getNickname()) ? registerDto.getNickname()
                : "用户_" + registerDto.getUsername());
        user.setPhone(registerDto.getPhone());
        // 默认角色为普通用户
        user.setRole(0);
        user.setGender(0); // 未知性别

        this.save(user);
    }

    @Override
    public String wxLogin(String code) {
        // TODO: 实现微信登录
        // 1. 使用code调用微信API获取openid
        // 2. 根据openid查找或创建用户
        // 3. 生成并返回JWT token
        throw new RuntimeException("微信登录功能尚未实现");
    }

    @Override
    public Page<WxUser> pageUsers(WxUserQueryDto queryDto) {
        Page<WxUser> page = new Page<>(queryDto.getPage(), queryDto.getPageSize());
        LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();

        // 按昵称模糊搜索
        if (StringUtils.hasText(queryDto.getNickname())) {
            queryWrapper.like(WxUser::getNickname, queryDto.getNickname());
        }

        // 按手机号模糊搜索
        if (StringUtils.hasText(queryDto.getPhone())) {
            queryWrapper.like(WxUser::getPhone, queryDto.getPhone());
        }

        // 按用户名模糊搜索
        if (StringUtils.hasText(queryDto.getUsername())) {
            queryWrapper.like(WxUser::getUsername, queryDto.getUsername());
        }

        // 按状态过滤
        if (queryDto.getStatus() != null) {
            // 根据状态过滤用户, 请确保数据库中存在对应字段
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(WxUser::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public void updateWxUser(WxUser wxUser) {
        WxUser existingUser = this.getById(wxUser.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新允许的字段
        if (StringUtils.hasText(wxUser.getNickname())) {
            existingUser.setNickname(wxUser.getNickname());
        }
        if (StringUtils.hasText(wxUser.getPhone())) {
            existingUser.setPhone(wxUser.getPhone());
        }
        if (wxUser.getGender() != null) {
            existingUser.setGender(wxUser.getGender());
        }
        if (wxUser.getRole() != null) {
            existingUser.setRole(wxUser.getRole());
        }

        this.updateById(existingUser);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        WxUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 目前WxUser实体已不包含状态字段,如需启用/禁用请在数据库及实体中补充实现
        this.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        WxUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 软删除逻辑根据业务需要在后续实现
        this.updateById(user);
    }

    @Override
    public String resetPassword(Long id) {
        WxUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 生成8位随机密码
        String newPassword = generateRandomPassword(8);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);

        return newPassword;
    }

    @Override
    public boolean isAdmin(Long userId) {
        WxUser user = this.getById(userId);
        if (user == null) {
            return false;
        }
        // 角色为1表示管理员
        return user.getRole() != null && user.getRole() == 1;
    }

    /**
     * 生成随机密码
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
