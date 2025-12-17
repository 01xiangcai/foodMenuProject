package com.yao.food_menu.enums;

import lombok.Getter;

import java.time.LocalTime;

/**
 * 餐次枚举
 */
@Getter
public enum MealPeriod {

    BREAKFAST("BREAKFAST", "早餐"),
    LUNCH("LUNCH", "中餐"),
    DINNER("DINNER", "晚餐");

    private final String code;
    private final String name;

    MealPeriod(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据代码获取餐次
     */
    public static MealPeriod fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (MealPeriod period : values()) {
            if (period.code.equals(code)) {
                return period;
            }
        }
        return null;
    }

    /**
     * 根据时间判断当前餐次
     * 
     * @param currentTime    当前时间
     * @param breakfastStart 早餐开始时间
     * @param lunchStart     中餐开始时间
     * @param dinnerStart    晚餐开始时间
     * @return 当前餐次
     */
    public static MealPeriod getCurrentPeriod(LocalTime currentTime,
            LocalTime breakfastStart,
            LocalTime lunchStart,
            LocalTime dinnerStart) {
        if (currentTime == null) {
            currentTime = LocalTime.now();
        }

        // 凌晨到早餐开始之前,显示早餐(为下一餐做准备)
        if (currentTime.isBefore(breakfastStart)) {
            return BREAKFAST;
        }

        // 如果当前时间在早餐开始时间之后且在中餐开始时间之前,则为早餐
        if ((currentTime.isAfter(breakfastStart) || currentTime.equals(breakfastStart))
                && currentTime.isBefore(lunchStart)) {
            return BREAKFAST;
        }

        // 如果当前时间在中餐开始时间之后且在晚餐开始时间之前,则为中餐
        if ((currentTime.isAfter(lunchStart) || currentTime.equals(lunchStart))
                && currentTime.isBefore(dinnerStart)) {
            return LUNCH;
        }

        // 晚餐开始时间之后,则为晚餐
        if (currentTime.isAfter(dinnerStart) || currentTime.equals(dinnerStart)) {
            return DINNER;
        }

        // 默认返回早餐
        return BREAKFAST;
    }
}
