package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 转正申请分页 VO
 */
@Data
@Schema(description = "转正申请分页记录")
public class RegularApplicationPageVO {

    /**
     * 转正申请ID，待转正员工未发起时为空
     */
    @Schema(description = "转正申请ID，待转正员工未发起时为空")
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
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String departmentName;

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID")
    private Long postId;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称")
    private String positionName;

    /**
     * 入职日期
     */
    @Schema(description = "入职日期")
    private LocalDate hireDate;

    /**
     * 试用期结束日期
     */
    @Schema(description = "试用期结束日期")
    private LocalDate probationEndDate;

    /**
     * 距试用期结束剩余天数
     */
    @Schema(description = "距试用期结束剩余天数")
    private Long remainingDays;

    /**
     * 评估状态：pending-待转正，evaluated-已评估
     */
    @Schema(description = "评估状态：pending-待转正，evaluated-已评估")
    private String evaluationStatus;

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
