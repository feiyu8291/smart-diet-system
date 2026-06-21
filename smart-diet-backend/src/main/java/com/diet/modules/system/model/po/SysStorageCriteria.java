package com.diet.modules.system.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 文件存储查询对象
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Data
public class SysStorageCriteria {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数据量", example = "10")
    private Integer size = 10;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "创建时间")
    private List<Timestamp> createTime;
}
