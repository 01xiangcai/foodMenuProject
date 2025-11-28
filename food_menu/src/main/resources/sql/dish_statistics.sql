-- 菜品统计表
DROP TABLE IF EXISTS `dish_statistics`;
CREATE TABLE `dish_statistics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
    `total_order_count` INT NOT NULL DEFAULT 0 COMMENT '总点单次数',
    `month_order_count` INT NOT NULL DEFAULT 0 COMMENT '本月点单次数',
    `week_order_count` INT NOT NULL DEFAULT 0 COMMENT '本周点单次数',
    `last_order_time` DATETIME NULL DEFAULT NULL COMMENT '最后点单时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_dish_id` (`dish_id` ASC) USING BTREE COMMENT '菜品ID唯一索引',
    INDEX `idx_total_count` (`total_order_count` DESC) USING BTREE COMMENT '总次数索引',
    INDEX `idx_month_count` (`month_order_count` DESC) USING BTREE COMMENT '月度次数索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜品统计表' ROW_FORMAT = Dynamic;

-- 初始化历史数据统计
-- 统计所有已完成订单(status=2或3)中的菜品点单次数
INSERT INTO dish_statistics (dish_id, total_order_count, last_order_time)
SELECT 
    oi.dish_id,
    COUNT(*) as total_count,
    MAX(o.create_time) as last_time
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
WHERE o.status IN (2, 3)
GROUP BY oi.dish_id
ON DUPLICATE KEY UPDATE
    total_order_count = VALUES(total_order_count),
    last_order_time = VALUES(last_order_time);
