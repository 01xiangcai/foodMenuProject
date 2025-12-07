package com.yao.food_menu.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.yao.food_menu.common.interceptor.FamilyDataInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();

        // 添加数据隔离拦截器（必须在分页拦截器之前）
        // 使用 TenantLineInnerInterceptor 包装我们的 Handler
        mybatisPlusInterceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new FamilyDataInterceptor()));

        // 添加乐观锁拦截器（用于@Version注解自动管理版本号）
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 添加分页拦截器
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        return mybatisPlusInterceptor;
    }
}
