package com.hrms.business.attendance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 考勤组分页查询参数。
 */
@Data
public class AttendanceGroupQueryDTO {

    /**
     * 考勤组名称关键字。
     */
    private String groupName;

    /**
     * 状态：1-启用，0-禁用。
     */
    private Integer status;

    /**
     * 当前页码。
     */
    @Min(value = 1, message = "pageNum必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页条数。
     */
    @Min(value = 1, message = "pageSize必须大于0")
    @Max(value = 100, message = "pageSize不能超过100")
    private Integer pageSize = 10;
}
