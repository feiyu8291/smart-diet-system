package com.diet.modules.biz.model.po;

import com.diet.modules.common.entity.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜品列表查询 PO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜谱分页查询条件实体类")
public class DietDishQueryPO extends BasePageQuery {

    @Schema(description = "做饭人或成员系统用户ID (关联sys_user.user_id)")
    private Long userId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "菜谱名称")
    private String dishName;
    @Schema(description = "菜系类型")
    private String cuisineType;
    @Schema(description = "就餐建议膳食模式")
    private Integer dietMode;
}
