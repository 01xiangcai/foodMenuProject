package com.yao.food_menu.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略接口
 * 支持多种存储方式: OSS、本地文件系统等
 */
public interface FileStorageStrategy {

    /**
     * 上传文件到指定文件夹
     * 
     * @param file       文件
     * @param folderType 文件夹类型 (GENERAL, AVATAR)
     * @return 文件存储路径/key
     */
    String upload(MultipartFile file, FolderType folderType);

    /**
     * 生成文件访问URL
     * 
     * @param fileKey 文件存储路径/key
     * @return 可访问的URL
     */
    String generateUrl(String fileKey);

    /**
     * 从URL中提取文件key
     * 
     * @param url URL或文件key
     * @return 文件key
     */
    String extractKey(String url);

    /**
     * 文件夹类型枚举
     */
    enum FolderType {
        GENERAL, // 通用文件夹
        AVATAR // 头像文件夹
    }
}
