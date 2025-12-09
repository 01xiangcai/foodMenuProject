package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.LoginDto;
import com.yao.food_menu.dto.WxUserQueryDto;
import com.yao.food_menu.entity.WxUser;

/**
 * 微信用户服务接口
 */
public interface WxUserService extends IService<WxUser> {

    /**
     * 发送验证码
     */
    void sendCode(String phone);

    /**
     * 用户登录
     */
    String login(LoginDto loginDto);

    /**
     * 获取当前用户
     */
    WxUser getCurrentUser(Long userId);

    /**
     * 用户注册
     */
    void register(com.yao.food_menu.dto.RegisterDto registerDto);

    /**
     * 微信登录(预留功能)
     */
    String wxLogin(String code);

    /**
     * 分页查询用户
     */
    Page<WxUser> pageUsers(WxUserQueryDto queryDto);

    /**
     * 更新微信用户(管理员)
     */
    void updateWxUser(WxUser wxUser);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 删除用户(软删除,将状态设置为0)
     */
    void deleteUser(Long id);

    /**
     * 重置用户密码
     */
    String resetPassword(Long id);

    /**
     * 判断是否为管理员(小程序端)
     */
    boolean isAdmin(Long userId);

    /**
     * 更新用户的家庭ID
     *
     * @param userId   用户ID
     * @param familyId 家庭ID
     */
    void updateUserFamily(Long userId, Long familyId);

    /**
     * 修改登录密码（用户自己操作）
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updateLoginPassword(Long userId, String oldPassword, String newPassword);

    /**
     * 修改用户密码(管理员操作)
     * 
     * @param userId      用户ID
     * @param newPassword 新密码
     */
    void updatePassword(Long userId, String newPassword);
}