package com.yao.food_menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI对话响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI对话响应")
public class AiChatResponse {

    @Schema(description = "AI回复内容")
    private String reply;

    @Schema(description = "推荐的菜品ID列表(可选)")
    private List<Long> suggestions;
}
