package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.RechargeDto;
import com.yao.food_menu.dto.WalletQueryDto;
import com.yao.food_menu.entity.UserWallet;
import com.yao.food_menu.entity.WalletTransaction;
import com.yao.food_menu.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端钱包控制器
 */
@RestController
@RequestMapping("/admin/wallet")
public class AdminWalletController {

    @Autowired
    private WalletService walletService;

    /**
     * 分页查询钱包列表
     */
    @GetMapping("/list")
    public Result<Page<UserWallet>> list(WalletQueryDto queryDto) {
        try {
            Page<UserWallet> page = walletService.pageWallets(queryDto);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 管理员充值
     */
    @PostMapping("/recharge")
    public Result<String> recharge(@RequestBody RechargeDto rechargeDto) {
        try {
            if (rechargeDto.getWxUserId() == null || rechargeDto.getWxUserId().isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            if (rechargeDto.getAmount() == null || rechargeDto.getAmount().doubleValue() <= 0) {
                return Result.error("充值金额必须大于0");
            }

            walletService.recharge(rechargeDto);
            return Result.success("充值成功");
        } catch (Exception e) {
            return Result.error("充值失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户钱包详情
     */
    @GetMapping("/detail/{wxUserId}")
    public Result<UserWallet> detail(@PathVariable String wxUserId) {
        try {
            UserWallet wallet = walletService.getOrCreateWallet(wxUserId);
            return Result.success(wallet);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户交易流水
     */
    @GetMapping("/transactions")
    public Result<Page<WalletTransaction>> transactions(
            @RequestParam String wxUserId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<WalletTransaction> pageResult = walletService.pageTransactions(wxUserId, page, pageSize);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
