package com.yao.food_menu.service.impl;

import com.yao.food_menu.service.OssService;
import com.yao.food_menu.service.storage.FileStorageFactory;
import com.yao.food_menu.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务实现
 * 使用策略模式支持多种存储方式
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OssServiceImpl implements OssService {

    private final FileStorageFactory storageFactory;

    @Override
    public String upload(MultipartFile file) {
        FileStorageStrategy strategy = storageFactory.getStrategy();
        return strategy.upload(file, FileStorageStrategy.FolderType.GENERAL);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        FileStorageStrategy strategy = storageFactory.getStrategy();
        return strategy.upload(file, FileStorageStrategy.FolderType.AVATAR);
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        FileStorageStrategy strategy = storageFactory.getStrategy();
        return strategy.generateUrl(objectKey);
    }

    @Override
    public String extractKeyFromUrl(String url) {
        FileStorageStrategy strategy = storageFactory.getStrategy();
        return strategy.extractKey(url);
    }
}
