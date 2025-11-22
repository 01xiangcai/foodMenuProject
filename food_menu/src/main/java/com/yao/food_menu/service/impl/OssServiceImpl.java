package com.yao.food_menu.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.yao.food_menu.common.config.AliyunOssProperties;
import com.yao.food_menu.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OssServiceImpl implements OssService {

    private final AliyunOssProperties ossProperties;

    @Override
    public String upload(MultipartFile file) {
        return uploadToFolder(file, ossProperties.getFolder());
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        return uploadToFolder(file, ossProperties.getAvatarFolder());
    }

    private String uploadToFolder(MultipartFile file, String folderConfig) {
        OSS ossClient = null;
        try {
            String clientEndpoint = normalizeEndpoint(ossProperties.getEndpoint());
            ossClient = new OSSClientBuilder()
                    .build(clientEndpoint, ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
            String folder = folderConfig;
            if (folder == null) {
                folder = "";
            }
            if (!folder.endsWith("/")) {
                folder = folder + "/";
            }
            String datePath = LocalDate.now().toString();
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            String objectName = folder + datePath + "/" + fileName;
            ossClient.putObject(ossProperties.getBucketName(), objectName, file.getInputStream());
            // Return object key (relative path) instead of URL
            // The frontend should save this to database, and convert to presigned URL when
            // displaying
            return objectName;
        } catch (IOException e) {
            throw new RuntimeException("Upload file to OSS failed", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        OSS ossClient = null;
        try {
            String clientEndpoint = normalizeEndpoint(ossProperties.getEndpoint());
            ossClient = new OSSClientBuilder()
                    .build(clientEndpoint, ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
            return generatePresignedUrl(ossClient, objectKey);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private String generatePresignedUrl(OSS ossClient, String objectKey) {
        Date expiration = new Date(
                System.currentTimeMillis() + ossProperties.getPresignedUrlExpirationHours() * 3600 * 1000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                ossProperties.getBucketName(), objectKey);
        request.setExpiration(expiration);
        URL url = ossClient.generatePresignedUrl(request);
        return url.toString();
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

    @Override
    public String extractKeyFromUrl(String url) {
        if (!StringUtils.hasText(url) || !url.startsWith("http")) {
            return url;
        }
        try {
            URL u = new URL(url);
            String path = u.getPath();
            // Remove leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (MalformedURLException e) {
            log.warn("Failed to parse URL to extract key: {}", url);
            return url;
        }
    }
}
