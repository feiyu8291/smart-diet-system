package com.diet.modules.common.entity;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 通用删除入参 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class BaseDeleteDTO implements Serializable {

    @Schema(description = "删除id")
    private Long id;

    @Schema(description = "删除id数组")
    private List<Long> ids;

    /**
     * 获取合并后的唯一 ID 集合
     */
    public Set<Long> allIds() {
        Set<Long> set = new HashSet<>();
        if (Objects.nonNull(id)) {
            set.add(id);
        }
        if (CollUtil.isNotEmpty(ids)) {
            ids.stream().filter(Objects::nonNull).forEach(set::add);
        }
        return set;
    }
}
