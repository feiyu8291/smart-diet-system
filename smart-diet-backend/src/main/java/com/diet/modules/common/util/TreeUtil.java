package com.diet.modules.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.diet.modules.common.entity.TreeNode;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UtilityClass
public class TreeUtil {

    /**
     * 方法描述：两层循环实现建树
     *
     * @param treeNodes 传入数据列表
     * @return java.util.List<T>
     * @author Fei_Yu
     * @date 2021/7/1 16:48
     */
    public <T extends TreeNode> List<T> buildTree(List<T> treeNodes) {
        // 将所有数据转换为Map结构，key为节点id value为
        Map<String, T> voMap =
                treeNodes.stream().collect(Collectors.toMap(T::getNodeCode, Function.identity(), (key1, key2) -> key1));
        List<T> resultTreeList = new ArrayList<>();
        for (T treeNode : treeNodes) {
            String parentNodeCode = treeNode.getParentCode();
            // 如果是首节点就加入返回的集合中
            if (CharSequenceUtil.isBlank(parentNodeCode) || "null".equals(parentNodeCode)) {
                resultTreeList.add(treeNode);
            } else {
                // 获取当前节点的父级节点集合，默认只有一条
                T parentNode = voMap.get(treeNode.getParentCode());
                // 理论上只有一条
                if (parentNode != null) {
                    // 上级的子节点中加入该节点
                    parentNode.add(treeNode);
                } else {
                    // 如果当前节点在所有节点中没有父节点，则将它设置为根节点
                    resultTreeList.add(treeNode);
                }
            }
        }
        return resultTreeList;
    }

    /**
     * 将扁平数据转换为树形结构，并支持自定义转换逻辑
     *
     * @param list      原始数据列表
     * @param converter 转换函数
     * @return 树形结构列表
     */
    public <E, V extends TreeNode> List<V> buildTree(List<E> list, Function<E, V> converter) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<V> nodes = list.stream().map(converter).collect(Collectors.toList());
        return buildTree(nodes);
    }

    /**
     * 通用转换：将实体属性拷贝到 VO，并支持自定义映射逻辑
     *
     * @param source         源对象
     * @param targetSupplier 目标对象提供者
     * @param extraMapping   额外映射逻辑（如字段名不一致的情况）
     * @return 转换后的目标对象
     */
    public <T> T copyProperties(Object source, Supplier<T> targetSupplier, Consumer<T> extraMapping) {
        if (source == null) {
            return null;
        }
        T target = targetSupplier.get();
        BeanUtil.copyProperties(source, target);
        if (extraMapping != null) {
            extraMapping.accept(target);
        }
        return target;
    }

}
