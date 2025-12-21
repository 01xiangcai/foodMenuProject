package com.yao.food_menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI对话请求DTO
 */
@Data
@Schema(description = "AI对话请求")
public class AiChatRequest {

    @Schema(description = "用户消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "是否包含历史对话", example = "true")
    private Boolean includeHistory = true;
}
