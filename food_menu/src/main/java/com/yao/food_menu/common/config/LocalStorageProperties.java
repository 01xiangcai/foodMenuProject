package com.yao.food_menu.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地文件存储配置
 */
@Component
@ConfigurationProperties(prefix = "file.storage.local")
@Data
public class LocalStorageProperties {

    /**
     * Local storage root directory (matches 'base-path' in yaml)
     */
    private String basePath = "E:/uploads/food-menu";

    /**
     * Access URL prefix
     */
    private String urlPrefix = "http://localhost:8080/uploads";

    /**
     * General folder name
     */
    private String folder = "food-menu";

    /**
     * Avatar folder name
     */
    private String avatarFolder = "avatars";
}
