package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 调岗申请分页 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "调岗申请分页记录")
public class TransferApplicationPageVO {

    /**
     * 调岗申请ID
     */
    @Schema(description = "调岗申请ID")
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
     * 员工工号
     */
    @Schema(description = "员工工号")
    private String employeeNo;

    /**
     * 原部门名称
     */
    @Schema(description = "原部门名称")
    private String fromDeptName;

    /**
     * 原岗位名称
     */
    @Schema(description = "原岗位名称")
    private String fromPostName;

    /**
     * 新部门名称
     */
    @Schema(description = "新部门名称")
    private String toDeptName;

    /**
     * 新岗位名称
     */
    @Schema(description = "新岗位名称")
    private String toPostName;

    /**
     * 生效日期
     */
    @Schema(description = "生效日期")
    private LocalDate effectiveDate;

    /**
     * 调岗原因
     */
    @Schema(description = "调岗原因")
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
