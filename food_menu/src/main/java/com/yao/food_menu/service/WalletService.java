package com.yao.food_menu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.food_menu.dto.PayDto;
import com.yao.food_menu.dto.RechargeDto;
import com.yao.food_menu.dto.WalletQueryDto;
import com.yao.food_menu.entity.UserWallet;
import com.yao.food_menu.entity.WalletTransaction;

/**
 * 钱包服务接口
 */
public interface WalletService extends IService<UserWallet> {

    /**
     * 分页查询钱包列表
     *
     * @param queryDto 查询参数
     * @return 分页结果
     */
    Page<UserWallet> pageWallets(WalletQueryDto queryDto);

    /**
     * 管理员充值
     *
     * @param rechargeDto 充值参数
     */
    void recharge(RechargeDto rechargeDto);

    /**
     * 获取用户钱包信息,如果不存在则自动创建
     *
     * @param wxUserId 微信用户ID
     * @return 钱包信息
     */
    UserWallet getOrCreateWallet(String wxUserId);

    /**
     * 获取用户钱包信息
     *
     * @param wxUserId 微信用户ID
     * @return 钱包信息, 可能为null
     */
    UserWallet getWalletByUserId(String wxUserId);

    /**
     * 余额支付
     *
     * @param wxUserId 微信用户ID
     * @param payDto   支付参数
     * @return 是否支付成功
     */
    boolean pay(String wxUserId, PayDto payDto);

    /**
     * 分页查询用户交易流水
     *
     * @param wxUserId 微信用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 流水分页结果
     */
    Page<WalletTransaction> pageTransactions(String wxUserId, Integer page, Integer pageSize);

    /**
     * 设置支付密码
     *
     * @param wxUserId    微信用户ID
     * @param payPassword 支付密码
     */
    void setPayPassword(String wxUserId, String payPassword);

    /**
     * 验证支付密码
     *
     * @param wxUserId    微信用户ID
     * @param payPassword 支付密码
     * @return 是否验证通过
     */
    boolean verifyPayPassword(String wxUserId, String payPassword);

    /**
     * 检查是否已设置支付密码
     *
     * @param wxUserId 微信用户ID
     * @return 是否已设置
     */
    boolean hasPayPassword(String wxUserId);
}
