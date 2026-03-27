package com.yao.food_menu.service.impl;

import com.yao.food_menu.common.config.LocalStorageProperties;
import com.yao.food_menu.common.util.FileValidationUtil;
import com.yao.food_menu.dto.MigrationStatus;
import com.yao.food_menu.service.ThumbnailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * 缩略图生成服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbnailServiceImpl implements ThumbnailService {

    private final LocalStorageProperties localProperties;
    
    // 任务运行状态与进度追踪
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private MigrationStatus currentStatus = new MigrationStatus();

    @Override
    public String generateThumbnail(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }

        // 统一处理路径分隔符
        relativePath = relativePath.replace("\\", "/");
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        // 如果已经是缩略图路径，则尝试提取原图路径
        String originalRelativePath;
        String thumbRelativePath;
        
        if (relativePath.contains("_thumb.")) {
            thumbRelativePath = relativePath;
            originalRelativePath = relativePath.replace("_thumb.", ".");
        } else {
            // 如果是原图路径，则构造缩略图路径
            originalRelativePath = relativePath;
            String extension = FileValidationUtil.getFileExtension(relativePath);
            String nameWithoutExt = relativePath.substring(0, relativePath.length() - extension.length() - (extension.isEmpty() ? 0 : 1));
            thumbRelativePath = nameWithoutExt + "_thumb." + (extension.isEmpty() ? "jpg" : extension);
        }

        Path thumbFullPath = Paths.get(localProperties.getBasePath(), thumbRelativePath);
        
        // 如果缩略图已存在，直接返回
        if (Files.exists(thumbFullPath)) {
            return thumbRelativePath;
        }

        // 缩略图不存在，尝试从原图生成
        Path originalFullPath = Paths.get(localProperties.getBasePath(), originalRelativePath);
        if (!Files.exists(originalFullPath)) {
            log.warn("无法生成缩略图，原图不存在: {}", originalFullPath);
            return null;
        }

        try {
            // 确保目录存在
            Files.createDirectories(thumbFullPath.getParent());
            
            // 生成缩略图
            Thumbnails.of(originalFullPath.toFile())
                    .size(400, 400)
                    .outputQuality(0.8)
                    .toFile(thumbFullPath.toFile());
            
            log.info("动态生成缩略图成功: {}", thumbRelativePath);
            return thumbRelativePath;
        } catch (IOException e) {
            log.error("生成缩略图失败: {}", relativePath, e);
            return null;
        }
    }

    @Override
    @Async
    public void batchGenerateThumbnails() {
        if (!isProcessing.compareAndSet(false, true)) {
            log.warn("缩略图生成任务已在运行中");
            return;
        }

        try {
            currentStatus = new MigrationStatus();
            currentStatus.setType("THUMBNAIL");
            currentStatus.setRunning(true);
            currentStatus.setTotal(0);
            currentStatus.setMigrated(0);
            currentStatus.setFailed(0);
            currentStatus.setSkipped(0);
            currentStatus.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            currentStatus.setMessage("正在统计缺失图片...");

            // 1. 预扫描统计总数
            int total = countMissingThumbnails();
            currentStatus.setTotal(total);
            
            if (total == 0) {
                currentStatus.setRunning(false);
                currentStatus.setProgress(100.0);
                currentStatus.setMessage("补全任务结束：没有发现缺失缩略图的原图");
                return;
            }

            // 2. 真正执行补全
            currentStatus.setMessage("正在批量补全缩略图...");
            processThumbnails(true, currentStatus);

            currentStatus.setRunning(false);
            currentStatus.setProgress(100.0);
            currentStatus.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            currentStatus.setMessage(String.format("任务已于 %s 成功结束", currentStatus.getEndTime()));
            
        } catch (Exception e) {
            log.error("缩略图批量任务执行异常", e);
            currentStatus.setRunning(false);
            currentStatus.setMessage("任务因异常中断: " + e.getMessage());
        } finally {
            isProcessing.set(false);
        }
    }

    @Override
    public MigrationStatus getThumbnailStatus() {
        return currentStatus;
    }

    @Override
    public int countMissingThumbnails() {
        return processThumbnails(false, null);
    }

    /**
     * 核心处理逻辑
     * @param executeGenerate 是否执行生成操作
     * @param status 用于反馈进度的状态对象 (可选)
     * @return 处理/统计的数量
     */
    private int processThumbnails(boolean executeGenerate, MigrationStatus status) {
        Path rootPath = Paths.get(localProperties.getBasePath());
        if (!Files.exists(rootPath)) {
            log.warn("根目录不存在: {}", rootPath);
            return 0;
        }

        AtomicInteger count = new AtomicInteger(0);
        try (Stream<Path> walk = Files.walk(rootPath)) {
            walk.filter(Files::isRegularFile)
                .filter(p -> FileValidationUtil.isImageFile(p.getFileName().toString()))
                .filter(p -> !p.getFileName().toString().contains("_thumb."))
                .forEach(originalFile -> {
                    String fileName = originalFile.getFileName().toString();
                    String extension = FileValidationUtil.getFileExtension(fileName);
                    String nameWithoutExt = fileName.substring(0, fileName.length() - extension.length() - (extension.isEmpty() ? 0 : 1));
                    String thumbFileName = nameWithoutExt + "_thumb." + (extension.isEmpty() ? "jpg" : extension);
                    
                    Path thumbFile = originalFile.getParent().resolve(thumbFileName);
                    if (!Files.exists(thumbFile)) {
                        if (executeGenerate) {
                            try {
                                Thumbnails.of(originalFile.toFile())
                                        .size(400, 400)
                                        .outputQuality(0.8)
                                        .toFile(thumbFile.toFile());
                                int currentCount = count.incrementAndGet();
                                log.debug("批量生成缩略图成功: {}", thumbFile);
                                // 更新实时进度
                                if (status != null && status.getTotal() > 0) {
                                    status.setMigrated(currentCount);
                                    status.setProgress(currentCount * 100.0 / status.getTotal());
                                }
                            } catch (IOException e) {
                                log.error("批量生成缩略图失败: {}", originalFile, e);
                                if (status != null) status.setFailed(status.getFailed() + 1);
                            }
                        } else {
                            count.incrementAndGet();
                        }
                    }
                });
        } catch (IOException e) {
            log.error("遍历目录失败", e);
        }
        
        return count.get();
    }
}
