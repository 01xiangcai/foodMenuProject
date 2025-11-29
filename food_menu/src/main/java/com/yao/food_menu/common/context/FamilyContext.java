package com.yao.food_menu.common.context;

/**
 * 家庭上下文工具类
 * 使用ThreadLocal存储当前请求的家庭ID和用户角色
 */
public class FamilyContext {

    private static final ThreadLocal<Long> FAMILY_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> USER_ROLE = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    /**
     * 设置家庭ID
     */
    public static void setFamilyId(Long familyId) {
        FAMILY_ID.set(familyId);
    }

    /**
     * 获取家庭ID
     */
    public static Long getFamilyId() {
        return FAMILY_ID.get();
    }

    /**
     * 设置用户角色
     */
    public static void setUserRole(Integer role) {
        USER_ROLE.set(role);
    }

    /**
     * 获取用户角色
     */
    public static Integer getUserRole() {
        return USER_ROLE.get();
    }

    /**
     * 设置用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 判断是否为超级管理员
     * 角色: 0-普通管理员, 1-家庭管理员, 2-超级管理员
     */
    public static boolean isSuperAdmin() {
        Integer role = USER_ROLE.get();
        return role != null && role == 2;
    }

    /**
     * 判断是否为家庭管理员
     */
    public static boolean isFamilyAdmin() {
        Integer role = USER_ROLE.get();
        return role != null && role == 1;
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        FAMILY_ID.remove();
        USER_ROLE.remove();
        USER_ID.remove();
    }
}
