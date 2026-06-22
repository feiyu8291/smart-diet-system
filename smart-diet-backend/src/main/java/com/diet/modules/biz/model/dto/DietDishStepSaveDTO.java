package com.diet.modules.biz.model.dto;

import lombok.Data;

/**
 * 菜品步骤关联保存 DTO
 *
 * @author FeiYu
 * @date 2026-06-22
 */
@Data
public class DietDishStepSaveDTO {
    private Long stepPoolId;
    private Integer stepNum;
    private String customDetail;
}
