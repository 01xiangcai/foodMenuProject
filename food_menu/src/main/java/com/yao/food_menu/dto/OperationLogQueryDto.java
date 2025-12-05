package com.yao.food_menu.dto;

import lombok.Data;

/**
 * 操作日志查询DTO
 * 
 * @author yao
 */
@Data
public class OperationLogQueryDto {
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 操作模块
     */
    private String operationModule;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 操作人姓名（模糊查询）
     */
    private String operatorName;
    
    /**
     * 操作人类型
     */
    private String operatorType;
    
    /**
     * 状态：0失败 1成功
     */
    private Integer status;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
    /**
     * IP地址
     */
    private String ipAddress;
}

