package com.yao.food_menu.common.config;

import com.yao.food_menu.common.util.StorageDirectoryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 存储初始化配置
 * 在应用启动时验证存储目录
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StorageInitializer implements ApplicationRunner {

    private final FileStorageProperties storageProperties;
    private final LocalStorageProperties localProperties;

    @Override
    public void run(ApplicationArguments args) {
        log.info("初始化文件存储配置...");
        log.info("当前存储类型: {}", storageProperties.getType());

        if (storageProperties.isLocal()) {
            initializeLocalStorage();
        } else if (storageProperties.isOss()) {
            log.info("使用OSS存储,无需初始化本地目录");
        } else {
            log.warn("未知的存储类型: {}, 将使用本地存储", storageProperties.getType());
            initializeLocalStorage();
        }
    }

    private void initializeLocalStorage() {
        String basePath = localProperties.getBasePath();
        log.info("本地存储根目录: {}", basePath);

        try {
            // 验证并创建基础目录
            StorageDirectoryUtil.validateDirectory(basePath);

            // 创建子目录
            String generalFolder = basePath + "/" + localProperties.getFolder();
            String avatarFolder = basePath + "/" + localProperties.getAvatarFolder();

            StorageDirectoryUtil.ensureDirectoryExists(generalFolder);
            StorageDirectoryUtil.ensureDirectoryExists(avatarFolder);

            // 检查可用空间
            long availableSpace = StorageDirectoryUtil.getAvailableSpace(basePath);
            if (availableSpace > 0) {
                long availableGB = availableSpace / (1024 * 1024 * 1024);
                log.info("存储目录可用空间: {}GB", availableGB);

                if (availableGB < 1) {
                    log.warn("⚠️ 磁盘可用空间不足1GB,请及时清理");
                }
            }

            log.info("✅ 本地存储初始化成功");
        } catch (Exception e) {
            log.error("❌ 本地存储初始化失败: {}", e.getMessage());
            log.error("请检查配置文件中的 file.upload.path 设置");
            throw new IllegalStateException("本地存储初始化失败", e);
        }
    }
}
