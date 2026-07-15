package com.hrms.business.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 请假申请创建请求。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCreateRequestDTO {

    /**
     * 请假类型字典ID。
     */
    private Long leaveTypeId;

    /**
     * 请假类型值，兼容前端 leaveType。
     */
    private String leaveType;

    /**
     * 开始日期。
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /**
     * 开始时段：AM/PM。
     */
    @NotBlank(message = "开始时段不能为空")
    private String startPeriod;

    /**
     * 结束日期。
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /**
     * 结束时段：AM/PM。
     */
    @NotBlank(message = "结束时段不能为空")
    private String endPeriod;

    /**
     * 请假原因。
     */
    private String reason;

    /**
     * 工作交接人员工ID，当前表无字段，暂不落库。
     */
    private Long handoverEmployeeId;

    /**
     * 附件文件ID。
     */
    private Long attachmentFileId;

    /**
     * 附件地址，兼容前端 attachment。
     */
    private String attachment;
}
