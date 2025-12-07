package com.yao.food_menu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.food_menu.common.util.JwtUtil;
import com.yao.food_menu.common.Result;
import com.yao.food_menu.dto.PayDto;
import com.yao.food_menu.entity.UserWallet;
import com.yao.food_menu.entity.WalletTransaction;
import com.yao.food_menu.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序端钱包控制器
 */
@RestController
@RequestMapping("/app/wallet")
public class AppWalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从Authorization header中提取token，移除Bearer前缀
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    /**
     * 获取当前用户钱包信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                return Result.error("请先登录");
            }

            UserWallet wallet = walletService.getOrCreateWallet(userId.toString());

            Map<String, Object> result = new HashMap<>();
            result.put("balance", wallet.getBalance());
            result.put("frozenAmount", wallet.getFrozenAmount());
            result.put("hasPayPassword", walletService.hasPayPassword(userId.toString()));

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取钱包信息失败: " + e.getMessage());
        }
    }

    /**
     * 余额支付
     */
    @PostMapping("/pay")
    public Result<String> pay(@RequestBody PayDto payDto, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                return Result.error("请先登录");
            }

            boolean success = walletService.pay(userId.toString(), payDto);
            if (success) {
                return Result.success("支付成功");
            } else {
                return Result.error("支付失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取交易流水
     */
    @GetMapping("/transactions")
    public Result<Page<WalletTransaction>> transactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                return Result.error("请先登录");
            }

            Page<WalletTransaction> pageResult = walletService.pageTransactions(userId.toString(), page, pageSize);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("获取流水失败: " + e.getMessage());
        }
    }

    /**
     * 设置支付密码
     */
    @PostMapping("/password")
    public Result<String> setPassword(@RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                return Result.error("请先登录");
            }

            String payPassword = body.get("payPassword");
            if (payPassword == null || payPassword.length() != 6) {
                return Result.error("支付密码必须为6位数字");
            }

            walletService.setPayPassword(userId.toString(), payPassword);
            return Result.success("设置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 验证支付密码
     */
    @PostMapping("/password/verify")
    public Result<Boolean> verifyPassword(@RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                return Result.error("请先登录");
            }

            String payPassword = body.get("payPassword");
            boolean verified = walletService.verifyPayPassword(userId.toString(), payPassword);
            return Result.success(verified);
        } catch (Exception e) {
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否已设置支付密码
     */
    @GetMapping("/password/check")
    public Result<Boolean> checkPassword(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                return Result.error("请先登录");
            }

            boolean hasPassword = walletService.hasPayPassword(userId.toString());
            return Result.success(hasPassword);
        } catch (Exception e) {
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 修改支付密码（需验证旧密码）
     */
    @PostMapping("/password/update")
    public Result<String> updatePassword(@RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                return Result.error("请先登录");
            }

            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");

            if (oldPassword == null || oldPassword.length() != 6) {
                return Result.error("旧密码格式不正确");
            }
            if (newPassword == null || newPassword.length() != 6) {
                return Result.error("新密码必须为6位数字");
            }

            walletService.updatePayPassword(userId.toString(), oldPassword, newPassword);
            return Result.success("修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
