package com.yao.food_menu.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 存储目录初始化工具类
 */
@Slf4j
public class StorageDirectoryUtil {

    /**
     * 确保目录存在,如果不存在则创建
     * 
     * @param directoryPath 目录路径
     * @return 是否成功
     */
    public static boolean ensureDirectoryExists(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);

            if (!Files.exists(path)) {
                log.info("创建存储目录: {}", directoryPath);
                Files.createDirectories(path);
                log.info("存储目录创建成功: {}", directoryPath);
                return true;
            }

            if (!Files.isDirectory(path)) {
                log.error("路径存在但不是目录: {}", directoryPath);
                return false;
            }

            if (!Files.isWritable(path)) {
                log.error("目录没有写入权限: {}", directoryPath);
                return false;
            }

            log.debug("存储目录已存在且可写: {}", directoryPath);
            return true;

        } catch (IOException e) {
            log.error("创建存储目录失败: {}", directoryPath, e);
            return false;
        }
    }

    /**
     * 验证目录是否可用
     * 
     * @param directoryPath 目录路径
     * @throws IllegalStateException 如果目录不可用
     */
    public static void validateDirectory(String directoryPath) {
        if (!ensureDirectoryExists(directoryPath)) {
            throw new IllegalStateException(
                    String.format("存储目录不可用: %s，请检查路径和权限", directoryPath));
        }
    }

    /**
     * 获取目录的可用空间(字节)
     * 
     * @param directoryPath 目录路径
     * @return 可用空间,失败返回-1
     */
    public static long getAvailableSpace(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (Files.exists(path)) {
                return Files.getFileStore(path).getUsableSpace();
            }
        } catch (IOException e) {
            log.error("获取目录可用空间失败: {}", directoryPath, e);
        }
        return -1;
    }

    /**
     * 检查是否有足够的磁盘空间
     * 
     * @param directoryPath 目录路径
     * @param requiredSpace 需要的空间(字节)
     * @return 是否有足够空间
     */
    public static boolean hasEnoughSpace(String directoryPath, long requiredSpace) {
        long availableSpace = getAvailableSpace(directoryPath);
        if (availableSpace == -1) {
            return false;
        }
        return availableSpace >= requiredSpace;
    }
}
