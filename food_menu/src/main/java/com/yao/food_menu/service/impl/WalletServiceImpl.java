package com.yao.food_menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.food_menu.dto.PayDto;
import com.yao.food_menu.dto.RechargeDto;
import com.yao.food_menu.dto.WalletQueryDto;
import com.yao.food_menu.entity.UserWallet;
import com.yao.food_menu.entity.WalletTransaction;
import com.yao.food_menu.entity.WxUser;
import com.yao.food_menu.mapper.UserWalletMapper;
import com.yao.food_menu.mapper.WalletTransactionMapper;
import com.yao.food_menu.mapper.WxUserMapper;
import com.yao.food_menu.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包服务实现类
 */
@Service
public class WalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWallet> implements WalletService {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private WalletTransactionMapper transactionMapper;

    @Autowired
    private WxUserMapper wxUserMapper;

    @Override
    public Page<UserWallet> pageWallets(WalletQueryDto queryDto) {
        Page<UserWallet> page = new Page<>(queryDto.getPage(), queryDto.getPageSize());

        LambdaQueryWrapper<UserWallet> wrapper = new LambdaQueryWrapper<>();

        // 按wxUserId模糊搜索
        if (StringUtils.hasText(queryDto.getWxUserId())) {
            wrapper.like(UserWallet::getWxUserId, queryDto.getWxUserId());
        }

        // 家庭数据隔离
        Long currentFamilyId = com.yao.food_menu.common.context.FamilyContext.getFamilyId();
        boolean isSuperAdmin = com.yao.food_menu.common.context.FamilyContext.isSuperAdmin();

        if (isSuperAdmin) {
            // 超级管理员：如果指定了家庭ID则按该家庭过滤，否则查看全部
            if (queryDto.getFamilyId() != null) {
                wrapper.eq(UserWallet::getFamilyId, queryDto.getFamilyId());
            }
        } else {
            // 非超级管理员：只能查看自己家庭的数据
            if (currentFamilyId != null) {
                wrapper.eq(UserWallet::getFamilyId, currentFamilyId);
            }
        }

        // 按更新时间倒序
        wrapper.orderByDesc(UserWallet::getUpdateTime);

        Page<UserWallet> result = this.page(page, wrapper);

        // 关联用户信息
        for (UserWallet wallet : result.getRecords()) {
            WxUser wxUser = wxUserMapper.selectOne(
                    new LambdaQueryWrapper<WxUser>()
                            .eq(WxUser::getId, wallet.getWxUserId()));
            if (wxUser != null) {
                wallet.setNickname(wxUser.getNickname());
                wallet.setPhone(wxUser.getPhone());
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(RechargeDto rechargeDto) {
        String wxUserId = rechargeDto.getWxUserId();
        BigDecimal amount = rechargeDto.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }

        // 获取或创建钱包
        UserWallet wallet = getOrCreateWallet(wxUserId);

        // 增加余额
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdateTime(LocalDateTime.now());

        // 使用乐观锁更新（@Version注解由插件自动处理版本号）
        boolean updated = this.updateById(wallet);
        if (!updated) {
            throw new RuntimeException("充值失败，请重试");
        }

        // 记录流水
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWxUserId(wxUserId);
        transaction.setTransType(WalletTransaction.TRANS_TYPE_RECHARGE);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setRemark(StringUtils.hasText(rechargeDto.getRemark()) ? rechargeDto.getRemark() : "管理员充值");
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setFamilyId(wallet.getFamilyId()); // 设置家庭ID

        transactionMapper.insert(transaction);
    }

    @Override
    public UserWallet getOrCreateWallet(String wxUserId) {
        UserWallet wallet = getWalletByUserId(wxUserId);
        WxUser wxUser = wxUserMapper.selectById(wxUserId);

        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setWxUserId(wxUserId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            wallet.setVersion(1);
            wallet.setCreateTime(LocalDateTime.now());
            wallet.setUpdateTime(LocalDateTime.now());

            if (wxUser != null && wxUser.getFamilyId() != null) {
                wallet.setFamilyId(wxUser.getFamilyId());
            }
            this.save(wallet);
        } else {
            // 同步家庭ID (如果用户切换了家庭)
            if (wxUser != null && wxUser.getFamilyId() != null
                    && !wxUser.getFamilyId().equals(wallet.getFamilyId())) {
                wallet.setFamilyId(wxUser.getFamilyId());
                wallet.setUpdateTime(LocalDateTime.now());
                this.updateById(wallet);
            }
        }
        return wallet;
    }

    @Override
    public UserWallet getWalletByUserId(String wxUserId) {
        return this.getOne(
                new LambdaQueryWrapper<UserWallet>()
                        .eq(UserWallet::getWxUserId, wxUserId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pay(String wxUserId, PayDto payDto) {
        BigDecimal amount = payDto.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("支付金额必须大于0");
        }

        // 获取钱包
        UserWallet wallet = getOrCreateWallet(wxUserId);

        // 校验支付密码
        if (StringUtils.hasText(wallet.getPayPassword())) {
            if (!StringUtils.hasText(payDto.getPayPassword())) {
                throw new RuntimeException("请输入支付密码");
            }
            if (!passwordEncoder.matches(payDto.getPayPassword(), wallet.getPayPassword())) {
                throw new RuntimeException("支付密码错误");
            }
        }

        // 校验可用余额 (余额 - 冻结金额)
        BigDecimal availableBalance = wallet.getBalance().subtract(wallet.getFrozenAmount());
        if (availableBalance.compareTo(amount) < 0) {
            throw new RuntimeException("余额不足");
        }

        // 使用乐观锁扣减余额
        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        LambdaUpdateWrapper<UserWallet> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserWallet::getId, wallet.getId())
                .eq(UserWallet::getVersion, wallet.getVersion())
                .set(UserWallet::getBalance, newBalance)
                .set(UserWallet::getVersion, wallet.getVersion() + 1)
                .set(UserWallet::getUpdateTime, LocalDateTime.now());

        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new RuntimeException("支付失败,请重试");
        }

        // 记录流水
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWxUserId(wxUserId);
        transaction.setTransType(WalletTransaction.TRANS_TYPE_CONSUME);
        transaction.setAmount(amount.negate()); // 消费金额为负数
        transaction.setBalanceAfter(newBalance);
        transaction.setRelatedOrderNo(payDto.getOrderNo());
        transaction.setRemark(StringUtils.hasText(payDto.getRemark()) ? payDto.getRemark() : "订单消费");
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setFamilyId(wallet.getFamilyId()); // 设置家庭ID

        transactionMapper.insert(transaction);

        return true;
    }

    @Override
    public Page<WalletTransaction> pageTransactions(String wxUserId, Integer page, Integer pageSize) {
        Page<WalletTransaction> pageParam = new Page<>(page, pageSize);

        // 获取用户当前家庭ID
        Long currentFamilyId = null;
        WxUser wxUser = wxUserMapper.selectById(wxUserId);
        if (wxUser != null) {
            currentFamilyId = wxUser.getFamilyId();
        }

        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletTransaction::getWxUserId, wxUserId);

        // 只显示当前家庭的流水
        if (currentFamilyId != null) {
            wrapper.eq(WalletTransaction::getFamilyId, currentFamilyId);
        }

        wrapper.orderByDesc(WalletTransaction::getCreateTime);

        return transactionMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPayPassword(String wxUserId, String payPassword) {
        if (!StringUtils.hasText(payPassword) || payPassword.length() != 6) {
            throw new RuntimeException("支付密码必须为6位数字");
        }

        // 验证是否为纯数字
        if (!payPassword.matches("\\d{6}")) {
            throw new RuntimeException("支付密码必须为6位数字");
        }

        UserWallet wallet = getOrCreateWallet(wxUserId);

        // 加密存储
        String encodedPassword = passwordEncoder.encode(payPassword);
        wallet.setPayPassword(encodedPassword);
        wallet.setUpdateTime(LocalDateTime.now());

        this.updateById(wallet);
    }

    @Override
    public boolean verifyPayPassword(String wxUserId, String payPassword) {
        UserWallet wallet = getWalletByUserId(wxUserId);
        if (wallet == null || !StringUtils.hasText(wallet.getPayPassword())) {
            return false;
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        Integer errorCount = wallet.getPayPasswordErrorCount();
        java.time.LocalDate errorDate = wallet.getPayPasswordErrorDate();

        // 如果是新的一天，重置错误计数
        if (errorDate == null || !errorDate.equals(today)) {
            errorCount = 0;
        }

        // 检查是否已达到当日错误上限(3次)
        if (errorCount != null && errorCount >= 3) {
            throw new RuntimeException("今日密码错误已达上限，请明天再试");
        }

        boolean matches = passwordEncoder.matches(payPassword, wallet.getPayPassword());

        if (!matches) {
            // 密码错误，在独立事务中更新错误计数（不受主事务回滚影响）
            int newErrorCount = (errorCount == null ? 0 : errorCount) + 1;
            updatePasswordErrorCount(wxUserId, newErrorCount, today);

            int remaining = 3 - newErrorCount;
            if (remaining > 0) {
                throw new RuntimeException("密码错误，今日还可尝试" + remaining + "次");
            } else {
                throw new RuntimeException("今日密码错误已达上限，请明天再试");
            }
        }

        // 密码正确，重置错误计数
        if (errorCount != null && errorCount > 0) {
            updatePasswordErrorCount(wxUserId, 0, today);
        }

        return true;
    }

    /**
     * 直接更新密码错误次数（绕过当前事务，使用独立更新）
     * 注意：同类内部调用@Transactional(REQUIRES_NEW)不生效，所以使用SQL直接更新
     */
    private void updatePasswordErrorCount(String wxUserId, int errorCount, java.time.LocalDate errorDate) {
        LambdaUpdateWrapper<UserWallet> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserWallet::getWxUserId, wxUserId)
                .set(UserWallet::getPayPasswordErrorCount, errorCount)
                .set(UserWallet::getPayPasswordErrorDate, errorDate)
                .set(UserWallet::getUpdateTime, LocalDateTime.now());
        this.baseMapper.update(null, updateWrapper);
    }

    @Override
    public boolean hasPayPassword(String wxUserId) {
        UserWallet wallet = getWalletByUserId(wxUserId);
        return wallet != null && StringUtils.hasText(wallet.getPayPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(String wxUserId, java.math.BigDecimal amount, String orderNo) {
        if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("退款金额必须大于0");
        }

        // 获取钱包
        UserWallet wallet = getOrCreateWallet(wxUserId);

        // 增加余额
        java.math.BigDecimal newBalance = wallet.getBalance().add(amount);

        // 使用乐观锁更新（@Version注解由插件自动处理版本号）
        wallet.setBalance(newBalance);
        wallet.setUpdateTime(java.time.LocalDateTime.now());

        boolean updated = this.updateById(wallet);
        if (!updated) {
            throw new RuntimeException("退款失败，请重试");
        }

        // 记录流水（退款作为充值类型，金额为正数）
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWxUserId(wxUserId);
        transaction.setTransType(WalletTransaction.TRANS_TYPE_REFUND);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setRelatedOrderNo(orderNo);
        transaction.setRemark("订单取消退款: " + orderNo);
        transaction.setCreateTime(java.time.LocalDateTime.now());
        transaction.setFamilyId(wallet.getFamilyId()); // 设置家庭ID

        transactionMapper.insert(transaction);
    }

    @Override
    public void updatePayPassword(String wxUserId, String oldPayPassword, String newPayPassword) {
        // 验证参数
        if (!StringUtils.hasText(oldPayPassword) || oldPayPassword.length() != 6) {
            throw new RuntimeException("旧密码格式不正确");
        }
        if (!StringUtils.hasText(newPayPassword) || newPayPassword.length() != 6) {
            throw new RuntimeException("新密码必须为6位数字");
        }

        // 获取钱包
        UserWallet wallet = getWalletByUserId(wxUserId);
        if (wallet == null) {
            throw new RuntimeException("钱包不存在");
        }

        // 验证旧密码
        if (!StringUtils.hasText(wallet.getPayPassword())) {
            throw new RuntimeException("尚未设置支付密码");
        }
        if (!passwordEncoder.matches(oldPayPassword, wallet.getPayPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 设置新密码
        wallet.setPayPassword(passwordEncoder.encode(newPayPassword));
        wallet.setUpdateTime(java.time.LocalDateTime.now());
        this.updateById(wallet);
    }

    @Override
    public void resetPayPassword(String wxUserId) {
        // 获取钱包
        UserWallet wallet = getWalletByUserId(wxUserId);
        if (wallet == null) {
            throw new RuntimeException("钱包不存在");
        }

        // 清空支付密码
        wallet.setPayPassword(null);
        wallet.setUpdateTime(java.time.LocalDateTime.now());
        this.updateById(wallet);
    }
}
