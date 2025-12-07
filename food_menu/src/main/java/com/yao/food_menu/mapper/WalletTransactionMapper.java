package com.yao.food_menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.food_menu.entity.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包流水 Mapper
 */
@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {
}
