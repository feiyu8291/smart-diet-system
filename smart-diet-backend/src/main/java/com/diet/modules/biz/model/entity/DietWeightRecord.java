package com.diet.modules.biz.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.diet.modules.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diet_weight_record")
@Schema(description = "成员体重记录历史实体类")
public class DietWeightRecord extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @Schema(description = "recordId")
    private Long recordId;
    @Schema(description = "对应 user_health_profile.profile_id")
    private Long profileId;
    @Schema(description = "recordWeight")
    private BigDecimal recordWeight;
    @Schema(description = "记录日期")
    private LocalDate recordDate;
    @Schema(description = "关联执行计划进度ID (0为日常测重)")
    private Long planProgressId;

}
