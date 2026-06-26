package com.diet.modules.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 经典加权随机算法工具类
 *
 * @author FeiYu
 * @date 2026-06-27
 */
public class WeightedRandomUtil {

    /**
     * 基于轮盘赌的加权随机无放回抽样算法
     *
     * @param candidateWeights 候选元素及其权重的映射 Map，权重必须大于等于 0
     * @param limit            抽取的最大元素个数
     * @param <T>              元素类型
     * @return 抽选出的元素列表（已按抽取顺序排列）
     */
    public static <T> List<T> selectWithoutReplacement(Map<T, Double> candidateWeights, int limit) {
        if (candidateWeights == null || candidateWeights.isEmpty() || limit <= 0) {
            return new ArrayList<>();
        }

        List<T> selected = new ArrayList<>();
        List<Map.Entry<T, Double>> entryList = new ArrayList<>(candidateWeights.entrySet());
        Random random = new Random();

        for (int i = 0; i < limit && !entryList.isEmpty(); i++) {
            double totalWeight = entryList.stream().mapToDouble(Map.Entry::getValue).sum();

            // 如果所有剩余项权重之和为 0，则退化为普通等概率随机抽取
            if (totalWeight <= 0) {
                int randIndex = random.nextInt(entryList.size());
                selected.add(entryList.get(randIndex).getKey());
                entryList.remove(randIndex);
                continue;
            }

            double randVal = random.nextDouble() * totalWeight;
            double currentSum = 0.0;
            int chosenIndex = 0;
            for (int k = 0; k < entryList.size(); k++) {
                currentSum += entryList.get(k).getValue();
                if (randVal <= currentSum) {
                    chosenIndex = k;
                    break;
                }
            }
            selected.add(entryList.get(chosenIndex).getKey());
            entryList.remove(chosenIndex);
        }

        return selected;
    }
}
