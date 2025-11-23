package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.dto.WxUserQueryDto;
import com.yao.food_menu.entity.WxUser;

/**
 * WxUser Service
 */
public interface WxUserService extends IService<WxUser> {

    /**
     * Send verification code
     */
    void sendCode(String phone);

    /**
     * User login
     */
    String login(LoginDto loginDto);

    /**
     * Get current user
     */
    WxUser getCurrentUser(Long userId);

    /**
     * User register
     */
    void register(com.yao.food_menu.dto.RegisterDto registerDto);

    /**
     * WeChat login (reserved for future implementation)
     */
    String wxLogin(String code);

    /**
     * Page query users
     */
    Page<WxUser> pageUsers(WxUserQueryDto queryDto);

    /**
     * Update wx user (for admin)
     */
    void updateWxUser(WxUser wxUser);

    /**
     * Update user status
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * Delete user (soft delete by setting status to 0)
     */
    void deleteUser(Long id);

    /**
     * Reset user password
     */
    String resetPassword(Long id);
}
