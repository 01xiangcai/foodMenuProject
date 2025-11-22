package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.entity.User;

public interface UserService extends IService<User> {

    /**
     * Send verification code (simulated)
     */
    void sendCode(String phone);

    /**
     * User login
     */
    String login(LoginDto loginDto);

    /**
     * Get current user info
     */
    User getCurrentUser(Long userId);
}
