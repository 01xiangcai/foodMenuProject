package com.yao.food_menu.common.util;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感信息脱敏转换器
 * 用于在日志输出时自动脱敏敏感信息
 * 
 * @author yao
 */
public class SensitiveDataConverter extends MessageConverter {
    
    // 手机号正则（中国大陆11位手机号）
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d)(\\d{4})(\\d{4})");
    
    // 密码相关关键词正则（不区分大小写）
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "(password|pwd|passwd|密码)[\"':=\\s]*(\\S+)",
        Pattern.CASE_INSENSITIVE
    );
    
    // 身份证号正则（18位）
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})\\d{8}(\\d{4})");
    
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w{1,3})\\w+(@\\w+\\.\\w+)");
    
    // 银行卡号正则（16-19位）
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})\\d{8,12}(\\d{4})");
    
    // JWT Token正则
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(token|authorization|bearer)[\"':=\\s]*(\\S+)", Pattern.CASE_INSENSITIVE);
    
    // 访问密钥正则（AccessKey、SecretKey等）
    private static final Pattern ACCESS_KEY_PATTERN = Pattern.compile(
        "(access[_-]?key|secret[_-]?key|api[_-]?key|app[_-]?secret)[\"':=\\s]*(\\S+)",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public String convert(ILoggingEvent event) {
        // 获取原始日志消息
        String originalMessage = event.getFormattedMessage();
        
        if (originalMessage == null || originalMessage.isEmpty()) {
            return originalMessage;
        }
        
        // 执行脱敏处理
        String desensitizedMessage = desensitize(originalMessage);
        
        return desensitizedMessage;
    }
    
    /**
     * 执行脱敏处理
     * 
     * @param message 原始消息
     * @return 脱敏后的消息
     */
    public static String desensitize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        String result = message;
        
        // 1. 脱敏手机号：保留前3位和后4位，中间4位用****代替
        result = desensitizePhone(result);
        
        // 2. 脱敏密码：完全隐藏
        result = desensitizePassword(result);
        
        // 3. 脱敏身份证号：保留前6位和后4位
        result = desensitizeIdCard(result);
        
        // 4. 脱敏邮箱：保留前3位和@后的域名
        result = desensitizeEmail(result);
        
        // 5. 脱敏银行卡号：保留前4位和后4位
        result = desensitizeBankCard(result);
        
        // 6. 脱敏Token
        result = desensitizeToken(result);
        
        // 7. 脱敏访问密钥
        result = desensitizeAccessKey(result);
        
        return result;
    }
    
    /**
     * 脱敏手机号
     * 例如：13812345678 -> 138****5678
     */
    private static String desensitizePhone(String message) {
        Matcher matcher = PHONE_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + "****" + matcher.group(3));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 脱敏密码
     * 例如：password=123456 -> password=******
     */
    private static String desensitizePassword(String message) {
        Matcher matcher = PASSWORD_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + "=******");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 脱敏身份证号
     * 例如：123456199001011234 -> 123456********1234
     */
    private static String desensitizeIdCard(String message) {
        Matcher matcher = ID_CARD_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + "********" + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 脱敏邮箱
     * 例如：example@test.com -> exa***@test.com
     */
    private static String desensitizeEmail(String message) {
        Matcher matcher = EMAIL_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + "***" + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 脱敏银行卡号
     * 例如：1234567890123456 -> 1234********3456
     */
    private static String desensitizeBankCard(String message) {
        Matcher matcher = BANK_CARD_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + "********" + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 脱敏Token
     * 例如：token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... -> token=eyJh****
     */
    private static String desensitizeToken(String message) {
        Matcher matcher = TOKEN_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String tokenValue = matcher.group(2);
            String masked = tokenValue.length() > 8 ? tokenValue.substring(0, 4) + "****" : "****";
            matcher.appendReplacement(sb, matcher.group(1) + "=" + masked);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 脱敏访问密钥
     * 例如：accessKey=LTAI5t123456 -> accessKey=LTAI****
     */
    private static String desensitizeAccessKey(String message) {
        Matcher matcher = ACCESS_KEY_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String keyValue = matcher.group(2);
            String masked = keyValue.length() > 8 ? keyValue.substring(0, 4) + "****" : "****";
            matcher.appendReplacement(sb, matcher.group(1) + "=" + masked);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 工具方法：手动脱敏手机号
     * 
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 工具方法：手动脱敏身份证号
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }
    
    /**
     * 工具方法：手动脱敏邮箱
     * 
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);
        
        if (prefix.length() <= 3) {
            return prefix.charAt(0) + "***" + suffix;
        }
        return prefix.substring(0, 3) + "***" + suffix;
    }
    
    /**
     * 工具方法：手动脱敏银行卡号
     * 
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }
        return bankCard.substring(0, 4) + "********" + bankCard.substring(bankCard.length() - 4);
    }
}

