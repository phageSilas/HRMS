package com.hrms.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户列表查询 DTO
 */
@Data
@Schema(description = "用户列表查询参数")
public class UserQueryDTO {

    @Schema(description = "当前页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，最大100", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "关键词搜索（匹配用户名/姓名/手机号）", example = "张三")
    private String keyword;

    @Schema(description = "状态筛选：1-启用 0-禁用", example = "1")
    private Integer status;

    @Schema(description = "按所属部门筛选", example = "1001")
    private Long deptId;

}
