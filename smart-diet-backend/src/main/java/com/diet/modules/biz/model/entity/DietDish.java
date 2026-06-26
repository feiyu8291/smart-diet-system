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
@TableName("diet_dish")
@Schema(description = "系统菜谱基本信息实体类")
public class DietDish extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "菜谱名称")
    private String dishName;
    @Schema(description = "封面图片ID")
    private Long coverImageId;

}
