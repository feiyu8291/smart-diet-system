package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_family_group")
@Schema(description = "家庭组档案实体类")
public class DietFamilyGroup extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "家庭组名称")
    private String groupName;
    @Schema(description = "创建人系统用户ID")
    private Long creatorUserId;
    @Schema(description = "菜品避重冷静天天数")
    private Integer cooldownDays;

}
