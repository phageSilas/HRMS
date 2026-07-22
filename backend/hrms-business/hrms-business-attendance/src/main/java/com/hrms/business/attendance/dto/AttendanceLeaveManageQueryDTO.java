package com.hrms.business.attendance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 请假管理列表查询参数。
 */
@Data
public class AttendanceLeaveManageQueryDTO {

    /**
     * 月份，格式为 yyyy-MM。
     */
    private String yearMonth;

    /**
     * 部门 ID。
     */
    private Long deptId;

    /**
     * 员工姓名或工号关键字。
     */
    private String keyword;

    /**
     * 审批状态。
     */
    private Integer approvalStatus;

    /**
     * 当前页码。
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页数量。
     */
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;

    /**
     * 是否强制刷新当前查询条件对应缓存。
     */
    private Boolean refreshCache;
}
