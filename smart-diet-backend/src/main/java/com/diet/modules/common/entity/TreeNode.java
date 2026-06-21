package com.diet.modules.common.entity;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 树形节点实体
 *
 * @author Fei_Yu
 * @date 2026/4/27 11:46
 */
@Schema(description = "树形节点")
@Getter
@Setter
public class TreeNode {

    @Schema(description = "节点编码 唯一值")
    private String nodeCode;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型(对应组织节点类型)")
    private Integer nodeType;

    @Schema(description = "父级节点编码 唯一值")
    private String parentCode;

    @Schema(description = "父级节点类型")
    private Integer parentType;

    @Schema(description = "子节点集合")
    protected List<TreeNode> children;

    public void add(TreeNode node) {
        if (CollUtil.isEmpty(children)) {
            children = new ArrayList<>();
        }
        children.add(node);
    }
}
