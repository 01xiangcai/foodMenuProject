package com.yao.food_menu.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;

/**
 * Web MVC配置
 * 配置本地文件的静态资源映射
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LocalStorageProperties localStorageProperties;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 配置本地文件访问路径
        // 访问路径: /uploads/**
        // 映射到: file:///E:/uploads/food-menu/
        String basePath = localStorageProperties.getBasePath();
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + basePath);
    }
}
