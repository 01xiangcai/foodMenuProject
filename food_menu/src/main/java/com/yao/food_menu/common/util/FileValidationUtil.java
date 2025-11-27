package com.yao.food_menu.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件验证工具类
 */
@Slf4j
public class FileValidationUtil {

    /**
     * 允许的图片文件扩展名
     */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"));

    /**
     * 允许的图片MIME类型
     */
    private static final Set<String> ALLOWED_IMAGE_MIME_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"));

    /**
     * 默认最大文件大小: 10MB
     */
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 验证图片文件
     * 
     * @param file 上传的文件
     * @throws IllegalArgumentException 如果文件不合法
     */
    public static void validateImageFile(MultipartFile file) {
        validateImageFile(file, DEFAULT_MAX_FILE_SIZE);
    }

    /**
     * 验证图片文件
     * 
     * @param file    上传的文件
     * @param maxSize 最大文件大小(字节)
     * @throws IllegalArgumentException 如果文件不合法
     */
    public static void validateImageFile(MultipartFile file, long maxSize) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            long maxSizeMB = maxSize / (1024 * 1024);
            throw new IllegalArgumentException(
                    String.format("文件大小超过限制，最大允许 %dMB", maxSizeMB));
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("不支持的文件类型: %s，仅支持: %s",
                            extension, String.join(", ", ALLOWED_IMAGE_EXTENSIONS)));
        }

        // 检查MIME类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("可疑的文件MIME类型: {}, 文件名: {}", contentType, originalFilename);
            // 不直接拒绝,因为某些浏览器可能发送不同的MIME类型
        }
    }

    /**
     * 安全化文件名
     * 移除特殊字符,限制长度
     * 
     * @param originalFilename 原始文件名
     * @return 安全的文件名
     */
    public static String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "unnamed";
        }

        // 移除路径分隔符,防止目录遍历攻击
        String filename = originalFilename.replaceAll("[/\\\\]", "");

        // 移除其他危险字符
        filename = filename.replaceAll("[<>:\"|?*]", "");

        // 限制文件名长度(不包括扩展名)
        String extension = getFileExtension(filename);
        String nameWithoutExt = filename.substring(0, filename.length() - extension.length() - 1);

        if (nameWithoutExt.length() > 50) {
            nameWithoutExt = nameWithoutExt.substring(0, 50);
        }

        // 重新组合文件名
        return nameWithoutExt + "." + extension;
    }

    /**
     * 获取文件扩展名
     * 
     * @param filename 文件名
     * @return 扩展名(小写,不包含点)
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 检查是否为图片文件
     * 
     * @param filename 文件名
     * @return 是否为图片
     */
    public static boolean isImageFile(String filename) {
        String extension = getFileExtension(filename);
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }
}
