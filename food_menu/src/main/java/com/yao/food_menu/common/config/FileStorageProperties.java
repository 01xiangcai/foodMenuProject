package com.yao.food_menu.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件存储类型配置
 */
@Component
@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageProperties {

    /**
     * 存储类型: oss 或 local
     */
    private String type = "local";

    /**
     * 判断是否使用OSS存储
     */
    public boolean isOss() {
        return "oss".equalsIgnoreCase(type);
    }

    /**
     * 判断是否使用本地存储
     */
    public boolean isLocal() {
        return "local".equalsIgnoreCase(type);
    }
}
