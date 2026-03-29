package com.yao.food_menu.common.config;

import com.yao.food_menu.common.interceptor.JwtTokenInterceptor;
import com.yao.food_menu.common.interceptor.UniappJwtInterceptor;
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
    private final UniappJwtInterceptor uniappJwtInterceptor;

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

        // 静态资源映射
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 注册小程序端JWT拦截器（优先级更高，专门处理/uniapp/**和/wx/**路径）
        registry.addInterceptor(uniappJwtInterceptor)
                .addPathPatterns("/uniapp/**", "/wx/**")
                .excludePathPatterns(
                        // 小程序端登录、注册和验证码接口
                        "/wx/user/login",
                        "/wx/user/register",
                        "/wx/user/sendcode",
                        "/public/**");

        // 注册后台管理端JWT拦截器
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 小程序端路径（由uniappJwtInterceptor处理）
                        "/uniapp/**",
                        "/wx/**",
                        // 后台管理端登录和验证码接口
                        "/user/login",
                        "/user/sendcode",
                        // 静态资源和文档
                        "/uploads/**",
                        "/static/**",
                        "/favicon.ico",
                        "/error",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/public/**");
    }
}
