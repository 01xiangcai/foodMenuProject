package com.yao.food_menu.common.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.yao.food_menu.common.context.FamilyContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 家庭数据隔离处理器
 * 实现MyBatis-Plus的多租户接口
 */
@Slf4j
public class FamilyDataInterceptor implements TenantLineHandler {

    /**
     * 需要进行数据隔离的表名列表
     */
    private static final List<String> FAMILY_TABLES = Arrays.asList(
            "user", "wx_user", "dish", "orders", "category",
            "banner", "dish_comment", "dish_favorite", "dish_flavor",
            "dish_tag", "dish_statistics", "order_items");

    @Override
    public Expression getTenantId() {
        Long familyId = FamilyContext.getFamilyId();
        if (familyId == null) {
            return new NullValue();
        }
        return new LongValue(familyId);
    }

    @Override
    public String getTenantIdColumn() {
        return "family_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 如果是超级管理员，跳过所有表的数据隔离
        if (FamilyContext.isSuperAdmin()) {
            return true;
        }

        // 如果是查询当前用户自己的信息，跳过user和wx_user表的数据隔离
        // 这样可以允许用户查询自己的信息，即使还没有绑定家庭
        if (FamilyContext.isQueryCurrentUser() && 
            ("user".equalsIgnoreCase(tableName) || "wx_user".equalsIgnoreCase(tableName))) {
            return true;
        }

        // 安全控制：只有在登录操作时，user和wx_user表才跳过数据隔离
        // 这样可以防止恶意调用其他接口绕过数据隔离
        if (FamilyContext.getFamilyId() == null) {
            // 只有登录操作时，user和wx_user表才跳过隔离（登录时需要查询所有用户进行验证）
            if (FamilyContext.isLoginOperation() && 
                ("user".equalsIgnoreCase(tableName) || "wx_user".equalsIgnoreCase(tableName))) {
                return true;
            }
            // 其他情况（非登录操作或非登录相关表）都应该被隔离
            // 返回false，让getTenantId返回NullValue，从而查不到任何数据
            return !FAMILY_TABLES.contains(tableName.toLowerCase());
        }

        // 只对指定表进行隔离
        // 移除表名的反引号和前缀等处理由MP自动完成，这里只需匹配表名
        return !FAMILY_TABLES.contains(tableName.toLowerCase());
    }
}
