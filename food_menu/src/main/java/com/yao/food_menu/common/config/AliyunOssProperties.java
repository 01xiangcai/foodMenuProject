package com.yao.food_menu.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class AliyunOssProperties {

    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String bucketName;

    private String folder;

    /**
     * Avatar folder for user avatar uploads
     */
    private String avatarFolder;

    /**
     * Optional custom domain (e.g. CDN). When not provided, public oss url will be
     * built automatically.
     */
    private String domain;

    /**
     * Presigned URL expiration time in hours. Default is 24 hours.
     */
    private Integer presignedUrlExpirationHours = 24;
}
