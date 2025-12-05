package com.yao.food_menu.common.annotation;

import java.lang.annotation.*;

/**
 * 敏感数据标记注解
 * 用于标记实体类中的敏感字段，在日志输出时自动脱敏
 * 
 * 使用示例：
 * <pre>
 * public class User {
 *     @SensitiveData(type = SensitiveType.PHONE)
 *     private String phone;
 *     
 *     @SensitiveData(type = SensitiveType.PASSWORD)
 *     private String password;
 * }
 * </pre>
 * 
 * @author yao
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveData {
    
    /**
     * 敏感数据类型
     */
    SensitiveType type();
    
    /**
     * 敏感数据类型枚举
     */
    enum SensitiveType {
        /**
         * 手机号：保留前3位和后4位，中间4位用****代替
         * 例如：13812345678 -> 138****5678
         */
        PHONE,
        
        /**
         * 密码：完全隐藏
         * 例如：123456 -> ******
         */
        PASSWORD,
        
        /**
         * 身份证号：保留前6位和后4位，中间8位用********代替
         * 例如：123456199001011234 -> 123456********1234
         */
        ID_CARD,
        
        /**
         * 邮箱：保留前3位和@后的域名，中间用***代替
         * 例如：example@test.com -> exa***@test.com
         */
        EMAIL,
        
        /**
         * 银行卡号：保留前4位和后4位，中间用********代替
         * 例如：1234567890123456 -> 1234********3456
         */
        BANK_CARD,
        
        /**
         * Token：只显示前4位，后面用****代替
         * 例如：eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... -> eyJh****
         */
        TOKEN,
        
        /**
         * 访问密钥：只显示前4位，后面用****代替
         * 例如：LTAI5t123456 -> LTAI****
         */
        ACCESS_KEY
    }
}

