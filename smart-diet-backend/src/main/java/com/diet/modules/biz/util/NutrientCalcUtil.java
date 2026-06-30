package com.diet.modules.biz.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 做法分支菜品营养素核算计算工具类
 *
 * @author FeiYu
 * @date 2026-06-30
 */
public class NutrientCalcUtil {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IngredientNutrientInfo {
        private BigDecimal useAmount; // 食材使用量 (g)
        private BigDecimal calories;   // 食材 100g 的卡路里 (kcal)
        private BigDecimal protein;    // 食材 100g 的蛋白质 (g)
        private BigDecimal fat;        // 食材 100g 的脂肪 (g)
        private BigDecimal carbs;      // 食材 100g 的碳水 (g)
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CalculatedNutrients {
        private BigDecimal calories = BigDecimal.ZERO;
        private BigDecimal protein = BigDecimal.ZERO;
        private BigDecimal fat = BigDecimal.ZERO;
        private BigDecimal carbs = BigDecimal.ZERO;
    }

    /**
     * 计算食材列表核算出的成品热量与三大营养素 (按 100g 成品标准计算)
     *
     * @param ingredients 食材原料详情列表
     * @return 最终 100g 成品的营养素和热量
     */
    public static CalculatedNutrients calculate(List<IngredientNutrientInfo> ingredients) {
        CalculatedNutrients result = new CalculatedNutrients();
        if (ingredients == null || ingredients.isEmpty()) {
            return result;
        }

        double totalWeight = 0;
        double totalCals = 0;
        double totalProt = 0;
        double totalFat = 0;
        double totalCarbs = 0;

        for (IngredientNutrientInfo item : ingredients) {
            if (item.getUseAmount() != null && item.getUseAmount().doubleValue() > 0) {
                double weight = item.getUseAmount().doubleValue();
                totalWeight += weight;

                double cals100 = item.getCalories() != null ? item.getCalories().doubleValue() : 0.0;
                double prot100 = item.getProtein() != null ? item.getProtein().doubleValue() : 0.0;
                double fat100 = item.getFat() != null ? item.getFat().doubleValue() : 0.0;
                double carbs100 = item.getCarbs() != null ? item.getCarbs().doubleValue() : 0.0;

                totalCals += (cals100 * weight) / 100.0;
                totalProt += (prot100 * weight) / 100.0;
                totalFat += (fat100 * weight) / 100.0;
                totalCarbs += (carbs100 * weight) / 100.0;
            }
        }

        if (totalWeight > 0) {
            // 折算为每 100g 成品的营养素及热量
            result.setCalories(BigDecimal.valueOf((totalCals / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
            result.setProtein(BigDecimal.valueOf((totalProt / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
            result.setFat(BigDecimal.valueOf((totalFat / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
            result.setCarbs(BigDecimal.valueOf((totalCarbs / totalWeight) * 100.0).setScale(2, RoundingMode.HALF_UP));
        }

        return result;
    }
}
