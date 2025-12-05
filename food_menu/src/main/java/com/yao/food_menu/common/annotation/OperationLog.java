package com.yao.food_menu.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 * 
 * 使用示例：
 * <pre>
 * @OperationLog(
 *     operationType = OperationType.INSERT,
 *     operationModule = "订单",
 *     operationDesc = "提交订单"
 * )
 * public Result submitOrder(OrderDto orderDto) {
 *     // 业务代码
 * }
 * </pre>
 * 
 * @author yao
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作类型
     */
    OperationType operationType() default OperationType.OTHER;
    
    /**
     * 操作模块（如：订单、菜品、用户等）
     */
    String operationModule() default "";
    
    /**
     * 操作描述
     */
    String operationDesc() default "";
    
    /**
     * 是否记录请求参数（默认记录）
     */
    boolean recordParams() default true;
    
    /**
     * 是否记录响应结果（默认不记录，避免数据量过大）
     */
    boolean recordResult() default false;
    
    /**
     * 操作类型枚举
     */
    enum OperationType {
        /**
         * 新增操作
         */
        INSERT("新增"),
        
        /**
         * 更新操作
         */
        UPDATE("修改"),
        
        /**
         * 删除操作
         */
        DELETE("删除"),
        
        /**
         * 查询操作
         */
        QUERY("查询"),
        
        /**
         * 登录操作
         */
        LOGIN("登录"),
        
        /**
         * 登出操作
         */
        LOGOUT("登出"),
        
        /**
         * 导出操作
         */
        EXPORT("导出"),
        
        /**
         * 导入操作
         */
        IMPORT("导入"),
        
        /**
         * 审核操作
         */
        AUDIT("审核"),
        
        /**
         * 授权操作
         */
        GRANT("授权"),
        
        /**
         * 其他操作
         */
        OTHER("其他");
        
        private final String description;
        
        OperationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
