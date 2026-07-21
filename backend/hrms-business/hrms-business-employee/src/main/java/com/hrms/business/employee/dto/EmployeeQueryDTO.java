package com.hrms.business.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工分页查询 DTO
 */
@Data
@Schema(description = "员工分页查询参数")
public class EmployeeQueryDTO {

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，最大100", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "关键词搜索（姓名/工号/手机号，模糊匹配）", example = "张三")
    private String keyword;

    @Schema(description = "部门ID列表（多选）", example = "[10, 101]")
    private List<Long> deptIds;

    @Schema(description = "在职状态多选：1-试用期 2-正式 3-待离职 4-已离职", example = "[1, 2]")
    private List<Integer> employmentStatus;

    @Schema(description = "职级筛选", example = "P5")
    private String jobLevel;

    @Schema(description = "入职日期范围-开始（yyyy-MM-dd）", example = "2024-01-01")
    private LocalDate hireDateStart;

    @Schema(description = "入职日期范围-结束（yyyy-MM-dd）", example = "2026-07-11")
    private LocalDate hireDateEnd;

    @Schema(description = "游标分页：上一页最后一条记录的ID（首次请求不传）", example = "900100")
    private Long lastId;
}
