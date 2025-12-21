package com.yao.food_menu.dto.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单解析结果
 */
@Data
public class OrderParseResult {

    /**
     * 解析成功的订单项
     */
    private List<OrderParseItem> items = new ArrayList<>();

    /**
     * 是否解析成功
     */
    private boolean success;

    /**
     * 错误信息(如果解析失败)
     */
    private String errorMessage;
}
