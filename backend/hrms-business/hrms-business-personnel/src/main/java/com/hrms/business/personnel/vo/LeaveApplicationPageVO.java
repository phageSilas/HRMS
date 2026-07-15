package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 离职申请分页 VO
 */
@Data
@Schema(description = "离职申请分页记录")
public class LeaveApplicationPageVO {

    /**
     * 离职申请ID
     */
    @Schema(description = "离职申请ID")
    private Long id;

    /**
     * 员工ID
     */
    @Schema(description = "员工ID")
    private Long employeeId;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名")
    private String employeeName;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String departmentName;

    /**
     * 离职类型编码
     */
    @Schema(description = "离职类型编码")
    private String leaveType;

    /**
     * 离职类型中文名
     */
    @Schema(description = "离职类型中文名")
    private String leaveTypeName;

    /**
     * 最后工作日
     */
    @Schema(description = "最后工作日")
    private LocalDate lastWorkDate;

    /**
     * 离职日期
     */
    @Schema(description = "离职日期")
    private LocalDate leaveDate;

    /**
     * 交接人姓名
     */
    @Schema(description = "交接人姓名")
    private String handoverEmployeeName;

    /**
     * 离职原因
     */
    @Schema(description = "离职原因")
    private String reason;

    /**
     * 审批状态
     */
    @Schema(description = "审批状态")
    private Integer approvalStatus;

    /**
     * 审批状态描述
     */
    @Schema(description = "审批状态描述")
    private String approvalStatusDesc;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
