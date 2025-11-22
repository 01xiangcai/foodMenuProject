package com.yao.food_menu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Upload result DTO containing object key and presigned URL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {
    /**
     * OSS object key (relative path), should be saved to database
     * e.g. "food-menu/2025/11/xxx.jpg"
     */
    private String objectKey;

    /**
     * Presigned URL for immediate preview (expires after configured hours)
     * e.g. "https://food-menu-yao.oss-cn-shenzhen.aliyuncs.com/food-menu/2025/11/xxx.jpg?Expires=..."
     */
    private String presignedUrl;
}

