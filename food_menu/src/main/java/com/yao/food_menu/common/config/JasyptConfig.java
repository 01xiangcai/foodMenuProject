package com.yao.food_menu.common.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jasypt配置加密配置类
 * 用于加密配置文件中的敏感信息
 */
@Configuration
@EnableEncryptableProperties
public class JasyptConfig {

    /**
     * 自定义StringEncryptor
     * 加密密钥通过环境变量或启动参数传入：-Djasypt.encryptor.password=your_password
     */
    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        
        // 加密密钥，从环境变量或系统属性读取
        // 生产环境必须通过环境变量设置：JASYPT_ENCRYPTOR_PASSWORD
        String password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        if (password == null) {
            password = System.getProperty("jasypt.encryptor.password", "DefaultPasswordChangeInProduction");
        }
        
        config.setPassword(password);
        // 加密算法
        config.setAlgorithm("PBEWithMD5AndDES");
        // 密钥获取迭代次数
        config.setKeyObtentionIterations("1000");
        // 连接池大小
        config.setPoolSize("1");
        // 盐值生成器
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        // 输出类型
        config.setStringOutputType("base64");
        
        encryptor.setConfig(config);
        return encryptor;
    }
}

