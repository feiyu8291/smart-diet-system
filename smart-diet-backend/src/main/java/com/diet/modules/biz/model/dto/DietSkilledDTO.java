package com.diet.modules.biz.model.dto;

import lombok.Data;

/**
 * 擅长/拿手菜管理 DTO
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class DietSkilledDTO {
    private Long userId;
    private Long dishId;
    private Integer isSkilled; // 1-是, 0-否
}
