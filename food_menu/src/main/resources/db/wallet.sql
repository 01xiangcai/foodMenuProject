-- =====================================================
-- 虚拟钱包与充值管理系统 数据库建表脚本
-- 创建日期: 2025-12-07
-- =====================================================

-- 用户钱包表
CREATE TABLE IF NOT EXISTS `wx_user_wallet` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `wx_user_id` VARCHAR(64) NOT NULL COMMENT '关联微信用户ID',
    `balance` DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
    `frozen_amount` DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    `pay_password` VARCHAR(255) DEFAULT NULL COMMENT '支付密码(BCrypt加密)',
    `version` INT NOT NULL DEFAULT 1 COMMENT '乐观锁版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wx_user_id` (`wx_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户钱包表';

-- 钱包流水表
CREATE TABLE IF NOT EXISTS `wx_wallet_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `wx_user_id` VARCHAR(64) NOT NULL COMMENT '关联微信用户ID',
    `trans_type` TINYINT NOT NULL COMMENT '交易类型: 1=后台充值, 2=订单消费, 3=退款冻结',
    `amount` DECIMAL(12, 2) NOT NULL COMMENT '变动金额',
    `balance_after` DECIMAL(12, 2) DEFAULT NULL COMMENT '交易后余额',
    `related_order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联业务单号',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_wx_user_id` (`wx_user_id`),
    KEY `idx_trans_type` (`trans_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钱包流水表';

-- =====================================================
-- 为所有现有微信用户初始化钱包
-- =====================================================
INSERT INTO `wx_user_wallet` (`wx_user_id`, `balance`, `frozen_amount`, `version`, `create_time`, `update_time`, `deleted`)
SELECT 
    CAST(id AS CHAR) AS wx_user_id,
    0.00 AS balance,
    0.00 AS frozen_amount,
    1 AS version,
    NOW() AS create_time,
    NOW() AS update_time,
    0 AS deleted
FROM `wx_user`
WHERE `deleted` = 0
  AND CAST(id AS CHAR) COLLATE utf8mb4_0900_ai_ci NOT IN (SELECT `wx_user_id` FROM `wx_user_wallet` WHERE `deleted` = 0);

