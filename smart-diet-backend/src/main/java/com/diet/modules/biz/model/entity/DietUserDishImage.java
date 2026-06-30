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
@TableName("diet_user_dish_image")
@Schema(description = "就餐打卡上传图片关联实体类")
public class DietUserDishImage extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "自制图ID")
    private Long imageId;
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "菜谱ID")
    private Long dishId;
    @Schema(description = "对应的存储记录文件ID")
    private Long storageId;

}
