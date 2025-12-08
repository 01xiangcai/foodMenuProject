package com.yao.food_menu;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.Test;

/**
 * Jasypt加密工具测试类
 * 用于生成加密后的配置值
 * 
 * 使用方法：
 * 1. 修改PASSWORD为你的加密密钥（生产环境要保密）
 * 2. 修改plainText为需要加密的明文
 * 3. 运行测试，复制加密结果到配置文件
 * 4. 配置文件中使用格式：ENC(加密结果)
 */
public class JasyptEncryptorTest {

    // 加密密钥（生产环境通过环境变量传入）
    private static final String PASSWORD = "FoodMenuSecretKey2024!@#";

    /**
     * 加密方法
     */
    private String encrypt(String plainText) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(PASSWORD);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor.encrypt(plainText);
    }

    /**
     * 解密方法（验证用）
     */
    private String decrypt(String encryptedText) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(PASSWORD);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor.decrypt(encryptedText);
    }

    @Test
    public void testEncrypt() {
        // 需要加密的明文列表
        String[] plainTexts = {
                // JWT密钥（建议64位以上）
                "YourJWTSecretKeyForProductionMustBeLongEnoughAndComplexMin64Chars!",
                // 数据库密码（开发环境）
                // "root",
                // 数据库密码（生产环境）
                "69fda4ae6385fde7"
                // OSS Access Key ID
                // "LTAI5tMRyKmbt7EZRgXjJRTk",
                // OSS Access Key Secret
                // "ueDZl8rmIIl4aVWzC7vM751eCZv0nf"
        };

        System.out.println("=== Jasypt加密结果 ===");
        System.out.println("加密密钥: " + PASSWORD);
        System.out.println();

        for (String plainText : plainTexts) {
            String encrypted = encrypt(plainText);
            System.out.println("明文: " + plainText);
            System.out.println("密文: " + encrypted);
            System.out.println("配置文件格式: ENC(" + encrypted + ")");

            // 验证解密
            String decrypted = decrypt(encrypted);
            System.out.println("解密验证: " + (decrypted.equals(plainText) ? "✓ 成功" : "✗ 失败"));
            System.out.println("---");
        }

        System.out.println("\n=== 使用说明 ===");
        System.out.println("1. 将加密结果复制到配置文件，格式为: ENC(密文)");
        System.out.println("2. 生产环境启动时设置环境变量: JASYPT_ENCRYPTOR_PASSWORD=" + PASSWORD);
        System.out.println("3. 或者使用启动参数: -Djasypt.encryptor.password=" + PASSWORD);
        System.out.println("4. 建议：生产环境使用更强的密钥，并妥善保管！");
    }
}
