package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 参与活动结果DTO
 */
@Data
public class ParticipateResultDto {

    // 是否中奖
    private Boolean isWin;

    // 中奖奖品信息
    private PrizeConfigDto prize;

    // 参与记录ID
    private Long recordId;

    // 提示消息
    private String message;

    // 剩余参与次数
    private Integer remainTimes;
}
