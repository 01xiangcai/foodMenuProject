package com.yao.food_menu.service.storage;

import com.yao.food_menu.common.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文件存储策略工厂
 * 根据配置返回对应的存储策略实例
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileStorageFactory {

    private final FileStorageProperties storageProperties;
    private final Map<String, FileStorageStrategy> strategyMap;

    /**
     * 获取当前配置的存储策略
     * 
     * @return 存储策略实例
     */
    public FileStorageStrategy getStrategy() {
        String type = storageProperties.getType();
        String strategyName = type + "StorageStrategy";

        FileStorageStrategy strategy = strategyMap.get(strategyName);
        if (strategy == null) {
            log.error("Unknown storage type: {}, falling back to local storage", type);
            strategy = strategyMap.get("localStorageStrategy");
        }

        log.debug("Using storage strategy: {}", strategyName);
        return strategy;
    }
}
