package com.yao.food_menu.service.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.yao.food_menu.common.config.AliyunOssProperties;
import com.yao.food_menu.common.util.FileValidationUtil;
import com.yao.food_menu.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云OSS存储策略实现
 */
@Component("ossStorageStrategy")
@RequiredArgsConstructor
@Slf4j
public class OssStorageStrategy implements FileStorageStrategy {

    @Override
    public String generateUrl(String objectKey) {
        OSS ossClient = null;
        try {
            String clientEndpoint = normalizeEndpoint(ossProperties.getEndpoint());
            ossClient = new OSSClientBuilder()
                    .build(clientEndpoint, ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());

            // 生成预签名URL
            Date expiration = new Date(
                    System.currentTimeMillis() + ossProperties.getPresignedUrlExpirationHours() * 3600 * 1000L);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    ossProperties.getBucketName(), objectKey);
            request.setExpiration(expiration);
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public String extractKey(String url) {
        if (!StringUtils.hasText(url) || !url.startsWith("http")) {
            return url;
        }
        try {
            URL u = new URL(url);
            String path = u.getPath();
            // 移除开头的斜杠
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (MalformedURLException e) {
            log.warn("Failed to parse URL to extract key: {}", url);
            return url;
        }
    }

    private String normalizeEndpoint(String endpoint) {
        if (!StringUtils.hasText(endpoint)) {
            throw new IllegalArgumentException("Aliyun OSS endpoint must not be empty");
        }
        String protocol = endpoint.startsWith("http://") ? "http://" : "https://";
        String host = extractHost(endpoint);
        if (host.startsWith(ossProperties.getBucketName() + ".")) {
            host = host.substring(ossProperties.getBucketName().length() + 1);
        }
        return protocol + host;
    }

    private String extractHost(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            return url.replace("https://", "").replace("http://", "");
        }
    }
}
