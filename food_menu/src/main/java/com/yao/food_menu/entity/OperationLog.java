package com.yao.food_menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 用于记录系统中的关键业务操作和管理员操作
 * 
 * @author yao
 */
@Data
@TableName("operation_log")
public class OperationLog {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 操作类型：INSERT/UPDATE/DELETE/QUERY/LOGIN/LOGOUT/EXPORT/IMPORT
     */
    private String operationType;
    
    /**
     * 操作模块：订单/菜品/用户/分类/轮播图等
     */
    private String operationModule;
    
    /**
     * 操作描述
     */
    private String operationDesc;
    
    /**
     * 方法名（包含类名）
     */
    private String methodName;
    
    /**
     * 请求方式：GET/POST/PUT/DELETE
     */
    private String requestMethod;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;
    
    /**
     * 响应结果（JSON格式）
     */
    private String responseResult;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 操作人姓名
     */
    private String operatorName;
    
    /**
     * 操作人类型：ADMIN(管理员)/USER(普通用户)/SYSTEM(系统)
     */
    private String operatorType;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理（浏览器信息）
     */
    private String userAgent;
    
    /**
     * 执行时长（毫秒）
     */
    private Long executionTime;
    
    /**
     * 状态：0失败 1成功
     */
    private Integer status;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 家庭ID
     */
    private Long familyId;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
