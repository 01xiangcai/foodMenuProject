package com.yao.food_menu.common;

/**
 * Based on ThreadLocal to encapsulate utility class, user to save and get the
 * current login user id
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * Set value
     * 
     * @param id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * Get value
     * 
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
