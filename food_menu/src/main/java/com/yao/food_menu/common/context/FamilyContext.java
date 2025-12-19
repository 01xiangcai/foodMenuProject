package com.yao.food_menu.common.context;

/**
 * 家庭上下文工具类
 * 使用ThreadLocal存储当前请求的家庭ID和用户角色
 * 
 * 注意：
 * - userId 用于后台管理端Admin用户
 * - wxUserId 用于小程序端WxUser用户
 */
public class FamilyContext {

    private static final ThreadLocal<Long> FAMILY_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> USER_ROLE = new ThreadLocal<>();
    // 后台管理员用户ID（admin端使用）
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    // 小程序微信用户ID（uniapp端使用）
    private static final ThreadLocal<Long> WX_USER_ID = new ThreadLocal<>();
    // 标记当前请求是否为登录操作（用于安全控制）
    private static final ThreadLocal<Boolean> IS_LOGIN_OPERATION = new ThreadLocal<>();
    // 标记当前请求是否为查询当前用户自己的信息（用于跳过数据隔离）
    private static final ThreadLocal<Boolean> IS_QUERY_CURRENT_USER = new ThreadLocal<>();

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
     * 获取当前家庭ID (getFamilyId的别名)
     */
    public static Long getCurrentFamilyId() {
        return getFamilyId();
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
     * 设置后台管理员用户ID（admin端使用）
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取后台管理员用户ID（admin端使用）
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置小程序微信用户ID（uniapp端使用）
     */
    public static void setWxUserId(Long wxUserId) {
        WX_USER_ID.set(wxUserId);
    }

    /**
     * 获取小程序微信用户ID（uniapp端使用）
     */
    public static Long getWxUserId() {
        return WX_USER_ID.get();
    }

    /**
     * 获取当前微信用户ID（getWxUserId的别名，语义更清晰）
     */
    public static Long getCurrentWxUserId() {
        return getWxUserId();
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
     * 设置是否为登录操作
     */
    public static void setLoginOperation(boolean isLogin) {
        IS_LOGIN_OPERATION.set(isLogin);
    }

    /**
     * 判断是否为登录操作
     */
    public static boolean isLoginOperation() {
        Boolean flag = IS_LOGIN_OPERATION.get();
        return flag != null && flag;
    }

    /**
     * 设置是否为查询当前用户自己的信息
     */
    public static void setQueryCurrentUser(boolean isQuery) {
        IS_QUERY_CURRENT_USER.set(isQuery);
    }

    /**
     * 判断是否为查询当前用户自己的信息
     */
    public static boolean isQueryCurrentUser() {
        Boolean flag = IS_QUERY_CURRENT_USER.get();
        return flag != null && flag;
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        FAMILY_ID.remove();
        USER_ROLE.remove();
        USER_ID.remove();
        WX_USER_ID.remove();
        IS_LOGIN_OPERATION.remove();
        IS_QUERY_CURRENT_USER.remove();
    }
}
