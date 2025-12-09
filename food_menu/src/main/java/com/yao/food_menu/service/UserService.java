package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.dto.UpdateProfileDto;
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

    /**
     * 修改用户密码(管理员操作)
     * 
     * @param userId      用户ID
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String newPassword);

    /**
     * 更新个人信息
     * 
     * @param updateProfileDto 个人信息DTO
     */
    void updateProfile(UpdateProfileDto updateProfileDto);

    /**
     * 修改个人密码
     * 
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updateOwnPassword(String oldPassword, String newPassword);
}
