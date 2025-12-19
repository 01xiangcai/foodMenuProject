package com.yao.food_menu.common.util;

import com.yao.food_menu.common.config.FileStorageProperties;
import com.yao.food_menu.common.config.LocalStorageProperties;
import com.yao.food_menu.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 图片URL处理工具类
 * 统一处理本地存储和OSS存储的图片URL转换
 *
 * @author yao
 */
@Slf4j
@Component
public class ImageUrlUtil {

    /**
     * 默认头像
     */
    public static final String DEFAULT_AVATAR = "/static/default-avatar.png";

    /**
     * 默认菜品图片
     */
    public static final String DEFAULT_DISH_IMAGE = "https://dummyimage.com/100x100/e2e8f0/94a3b8&text=No+Image";

    private final LocalStorageProperties localStorageProperties;
    private final FileStorageProperties fileStorageProperties;
    private final OssService ossService;

    public ImageUrlUtil(LocalStorageProperties localStorageProperties,
            FileStorageProperties fileStorageProperties,
            OssService ossService) {
        this.localStorageProperties = localStorageProperties;
        this.fileStorageProperties = fileStorageProperties;
        this.ossService = ossService;
    }

    /**
     * 处理图片URL，根据存储方式转换为完整URL
     * 如果图片为空，返回null
     *
     * @param image 原始图片路径
     * @return 完整的图片URL，如果原始路径为空则返回null
     */
    public String processUrl(String image) {
        return processUrl(image, null);
    }

    /**
     * 处理图片URL，根据存储方式转换为完整URL
     *
     * @param image        原始图片路径
     * @param defaultImage 默认图片URL（当原始路径为空时使用）
     * @return 完整的图片URL
     */
    public String processUrl(String image, String defaultImage) {
        if (!StringUtils.hasText(image)) {
            return defaultImage;
        }

        // 如果已经是完整URL，直接返回
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }

        // 根据存储方式处理
        if (fileStorageProperties.isLocal()) {
            // 本地存储模式：拼接URL前缀
            String urlPrefix = localStorageProperties.getUrlPrefix();
            if (!urlPrefix.endsWith("/")) {
                urlPrefix += "/";
            }
            // 移除image开头的斜杠
            String localPath = image.startsWith("/") ? image.substring(1) : image;
            return urlPrefix + localPath;
        } else {
            // OSS存储模式：转换为预签名URL
            try {
                return ossService.generatePresignedUrl(image);
            } catch (Exception e) {
                log.warn("生成图片预签名URL失败: {}", image, e);
                return defaultImage;
            }
        }
    }

    /**
     * 处理头像URL
     *
     * @param avatar 原始头像路径
     * @return 完整的头像URL，如果原始路径为空则返回默认头像
     */
    public String processAvatarUrl(String avatar) {
        return processUrl(avatar, DEFAULT_AVATAR);
    }

    /**
     * 处理菜品图片URL
     *
     * @param dishImage 原始菜品图片路径
     * @return 完整的菜品图片URL
     */
    public String processDishImageUrl(String dishImage) {
        return processUrl(dishImage, DEFAULT_DISH_IMAGE);
    }
}
