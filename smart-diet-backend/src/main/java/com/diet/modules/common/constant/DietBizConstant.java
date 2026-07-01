package com.diet.modules.common.constant;

import lombok.Getter;

/**
 * 业务数据与数据字典常量定义类
 *
 * @author FeiYu
 * @date 2026-06-30
 */
public class DietBizConstant {

    public static final String GRAM_UNIT = " 克";
    public static final String PORTION_NOTE = "根据个人健康目标量身定制的分量配比";
    public static final String DEFAULT_DISH_NAME = "营养配餐";
    public static final String DEFAULT_DISH_NOTE = "分配份量";

    /**
     * 餐次枚举定义
     */
    @Getter
    public enum MealPeriod {
        BREAKFAST(1, "早餐 🥞"),
        LUNCH(2, "午餐 🥗"),
        DINNER(3, "晚餐 🐟");

        private final int code;
        private final String name;

        MealPeriod(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getNameByCode(Integer code) {
            if (code == null) {
                return "";
            }
            for (MealPeriod period : MealPeriod.values()) {
                if (period.code == code) {
                    return period.name;
                }
            }
            return "";
        }
    }

    /**
     * 膳食模式枚举定义
     */
    @Getter
    public enum DietMode {
        NORMAL(0, "正常饮食"),
        LIGHT(1, "轻食减脂"),
        CHEAT(2, "放纵餐");

        private final int code;
        private final String name;

        DietMode(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getNameByCode(Integer code) {
            if (code == null) {
                return NORMAL.name;
            }
            for (DietMode mode : DietMode.values()) {
                if (mode.code == code) {
                    return mode.name;
                }
            }
            return NORMAL.name;
        }
    }

    private DietBizConstant() {
    }
}
