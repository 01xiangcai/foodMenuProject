package com.yao.food_menu.service.storage.impl;

import com.yao.food_menu.common.config.LocalStorageProperties;
import com.yao.food_menu.common.util.FileValidationUtil;
import com.yao.food_menu.common.util.StorageDirectoryUtil;
import com.yao.food_menu.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 本地文件系统存储策略实现
 */
@Component("localStorageStrategy")
@RequiredArgsConstructor
@Slf4j
public class LocalStorageStrategy implements FileStorageStrategy {

    private final LocalStorageProperties localProperties;

    @Override
    public String upload(MultipartFile file, FolderType folderType) {
        try {
            // 1. 验证文件(类型、大小)
            FileValidationUtil.validateImageFile(file);

            // 2. 安全化文件名
            String originalFilename = file.getOriginalFilename();
            String safeFilename = FileValidationUtil.sanitizeFilename(originalFilename);

            // 3. 根据文件夹类型选择路径
            String folder = folderType == FolderType.AVATAR
                    ? localProperties.getAvatarFolder()
                    : localProperties.getFolder();

            // 4. 按日期组织文件
            String datePath = LocalDate.now().toString();
            String fileName = UUID.randomUUID() + "-" + safeFilename;

            // 5. 构建相对路径 (用于存储到数据库)
            String relativePath = folder + "/" + datePath + "/" + fileName;

            // 6. 构建完整的文件系统路径
            Path fullPath = Paths.get(localProperties.getBasePath(), relativePath);

            // 7. 确保目录存在
            Files.createDirectories(fullPath.getParent());

            // 8. 检查磁盘空间(预留100MB)
            if (!StorageDirectoryUtil.hasEnoughSpace(
                    localProperties.getBasePath(), file.getSize() + 100 * 1024 * 1024)) {
                log.warn("磁盘空间不足,但仍尝试保存文件");
            }

            // 9. 保存文件
            file.transferTo(fullPath.toFile());

            log.info("文件上传成功 - 类型: {}, 大小: {}KB, 路径: {}",
                    FileValidationUtil.getFileExtension(safeFilename),
                    file.getSize() / 1024,
                    fullPath);

            // 10. 返回相对路径
            return relativePath;
        } catch (IllegalArgumentException e) {
            // 文件验证失败
            log.error("文件验证失败: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("文件上传失败 - 文件名: {}, 错误: {}",
                    file.getOriginalFilename(), e.getMessage());
            throw new RuntimeException("文件上传到本地存储失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateUrl(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            return "";
        }

        // 如果已经是完整URL,直接返回
        if (fileKey.startsWith("http")) {
            return fileKey;
        }

        // 拼接URL前缀
        String urlPrefix = localProperties.getUrlPrefix();
        if (!urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix + "/";
        }

        // 移除fileKey开头的斜杠
        String key = fileKey.startsWith("/") ? fileKey.substring(1) : fileKey;

        return urlPrefix + key;
    }

    @Override
    public String extractKey(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }

        // 如果不是URL,直接返回
        if (!url.startsWith("http")) {
            return url;
        }

        // 提取URL中的路径部分
        String urlPrefix = localProperties.getUrlPrefix();
        if (url.startsWith(urlPrefix)) {
            String key = url.substring(urlPrefix.length());
            // 移除开头的斜杠
            return key.startsWith("/") ? key.substring(1) : key;
        }

        return url;
    }
}
