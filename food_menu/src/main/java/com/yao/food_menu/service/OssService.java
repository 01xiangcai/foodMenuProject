package com.yao.food_menu.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {

    /**
     * Upload file to OSS and return object key (relative path)
     * @return OSS object key (e.g. "food-menu/2025/11/xxx.jpg")
     */
    String upload(MultipartFile file);

    /**
     * Generate presigned URL for existing OSS object
     * @param objectKey OSS object key (e.g. "food-menu/2025/11/xxx.jpg")
     * @return Presigned URL with expiration
     */
    String generatePresignedUrl(String objectKey);
}

