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
     * 本地存储根目录
     */
    private String basePath = "E:/uploads/food-menu";

    /**
     * 访问URL前缀
     */
    private String urlPrefix = "http://localhost:8080/uploads";

    /**
     * 通用文件夹
     */
    private String folder = "food-menu";

    /**
     * 头像文件夹
     */
    private String avatarFolder = "avatars";
}
