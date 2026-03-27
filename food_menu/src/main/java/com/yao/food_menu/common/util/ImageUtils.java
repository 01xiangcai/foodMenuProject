package com.yao.food_menu.common.util;

import com.yao.food_menu.common.config.LocalStorageProperties;
import org.springframework.util.StringUtils;

/**
 * 图片处理工具类
 */
public class ImageUtils {

    /**
     * 处理图片URL，如果是相对路径则拼接前缀，如果是完整URL则直接返回
     * 
     * @param path 原路径
     * @param urlPrefix URL前缀
     * @param useThumb 是否使用缩略图
     * @return 完整URL
     */
    public static String processImageUrl(String path, String urlPrefix, boolean useThumb) {
        if (!StringUtils.hasText(path)) {
            return null;
        }

        // 1. 如果是完整URL，直接返回
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("data:")) {
            return path;
        }

        // 2. 相对路径处理
        String processedPath = path;
        if (useThumb) {
            String ext = FileValidationUtil.getFileExtension(path);
            if (StringUtils.hasText(ext)) {
                processedPath = path.substring(0, path.length() - ext.length() - 1) + "_thumb." + ext;
            }
        }

        // 拼接前缀
        if (!StringUtils.hasText(urlPrefix)) {
            return processedPath;
        }

        String finalPrefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
        String cleanPath = processedPath.startsWith("/") ? processedPath.substring(1) : processedPath;
        
        return finalPrefix + cleanPath;
    }

    /**
     * 处理图片URL（不使用缩略图）
     */
    public static String processImageUrl(String path, String urlPrefix) {
        return processImageUrl(path, urlPrefix, false);
    }
}
