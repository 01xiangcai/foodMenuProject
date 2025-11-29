package com.yao.food_menu.common.config;

import com.yao.food_menu.common.interceptor.JwtTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;

/**
 * Web MVC配置
 * 配置本地文件的静态资源映射和拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LocalStorageProperties localStorageProperties;
    private final JwtTokenInterceptor jwtTokenInterceptor;

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

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 注册JWT拦截器
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/admin/user/login",
                        "/uniapp/user/login",
                        "/uniapp/user/register",
                        "/uploads/**",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**");
    }
}
