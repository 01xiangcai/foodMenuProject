package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.dto.UserDto;
import com.yao.food_menu.dto.UserQueryDto;
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

    /**
     * Page query users
     */
    Page<User> pageUsers(UserQueryDto queryDto);

    /**
     * Create user
     */
    void createUser(UserDto userDto);

    /**
     * Update user (for admin)
     */
    void updateUser(UserDto userDto);

    /**
     * Delete user (soft delete)
     */
    void deleteUser(Long id);

    /**
     * Update user status
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * Reset user password
     */
    String resetPassword(Long id);
}
