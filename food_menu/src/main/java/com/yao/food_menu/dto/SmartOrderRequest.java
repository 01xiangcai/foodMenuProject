package com.yao.food_menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能下单请求DTO
 */
@Data
@Schema(description = "智能下单请求")
public class SmartOrderRequest {

    @Schema(description = "用户输入的文本", requiredMode = Schema.RequiredMode.REQUIRED, example = "我要两份宫保鸡丁和一份鱼香肉丝")
    private String text;
}
