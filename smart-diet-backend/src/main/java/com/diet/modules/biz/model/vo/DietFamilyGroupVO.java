package com.diet.modules.biz.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家庭分组视图 VO
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Data
@Schema(description = "家庭分组视图对象")
public class DietFamilyGroupVO {
    @Schema(description = "家庭组ID")
    private Long groupId;
    @Schema(description = "家庭组名称")
    private String groupName;
    @Schema(description = "创建人系统用户ID")
    private Long creatorUserId;
    @Schema(description = "菜品避重冷静天天数")
    private Integer cooldownDays;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
