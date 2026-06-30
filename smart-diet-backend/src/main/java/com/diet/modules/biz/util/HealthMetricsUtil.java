package com.diet.modules.biz.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 健康指标评估核心计算工具类 (无状态，只负责算法逻辑)
 *
 * @author FeiYu
 * @date 2026-06-30
 */
public class HealthMetricsUtil {

    /**
     * 计算基础代谢率 (BMR) - Harris-Benedict 公式
     *
     * @param weight 体重 (kg)
     * @param height 身高 (cm)
     * @param age    年龄 (岁)
     * @param gender 性别 (1-男, 2-女)
     * @return BMR (kcal)
     */
    public static BigDecimal calculateBmr(double weight, double height, int age, int gender) {
        double bmr;
        if (gender == 1) { // 男
            bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else { // 女
            bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        }
        return BigDecimal.valueOf(bmr).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算每日总能量消耗 (TDEE)
     *
     * @param bmr           基础代谢率 (kcal)
     * @param activityLevel 活动强度等级 (1-久坐, 2-轻度, 3-中度, 4-重度)
     * @return TDEE (kcal)
     */
    public static BigDecimal calculateTdee(BigDecimal bmr, Integer activityLevel) {
        if (bmr == null) {
            return BigDecimal.ZERO;
        }
        double bmrVal = bmr.doubleValue();
        double tdeeFactor = 1.2;
        if (activityLevel != null) {
            switch (activityLevel) {
                case 1:
                    tdeeFactor = 1.2;
                    break; // 久坐
                case 2:
                    tdeeFactor = 1.375;
                    break; // 轻度活动
                case 3:
                    tdeeFactor = 1.55;
                    break; // 中度活动
                case 4:
                    tdeeFactor = 1.725;
                    break; // 重度活动
                default:
                    tdeeFactor = 1.2;
            }
        }
        double tdee = bmrVal * tdeeFactor;
        return BigDecimal.valueOf(tdee).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算推荐每日目标摄入热量 (Daily Target Calories)
     *
     * @param bmr       基础代谢率 (kcal)
     * @param tdee      每日总消耗 (kcal)
     * @param dietSpeed 周科学减重速度 (kg/周)
     * @return 每日推荐目标摄入热量 (kcal)
     */
    public static BigDecimal calculateDailyTargetCalories(BigDecimal bmr, BigDecimal tdee, BigDecimal dietSpeed) {
        if (bmr == null || tdee == null) {
            return BigDecimal.ZERO;
        }
        double bmrVal = bmr.doubleValue();
        double tdeeVal = tdee.doubleValue();
        double dietSpeedVal = (dietSpeed != null) ? dietSpeed.doubleValue() : 0.5;
        double calorieDeficit = dietSpeedVal * 1100.0;
        double dailyTarget = tdeeVal - calorieDeficit;

        // 设置安全红线：不低于 BMR 的 90%，且绝对不低于 1000 kcal
        double safetyLimit = Math.max(bmrVal * 0.9, 1000.0);
        if (dailyTarget < safetyLimit) {
            dailyTarget = safetyLimit;
        }
        return BigDecimal.valueOf(dailyTarget).setScale(2, RoundingMode.HALF_UP);
    }
}
