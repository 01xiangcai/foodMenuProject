package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.LoginDto;
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
     * WeChat login (reserved for future implementation)
     */
    String wxLogin(String code);
}
