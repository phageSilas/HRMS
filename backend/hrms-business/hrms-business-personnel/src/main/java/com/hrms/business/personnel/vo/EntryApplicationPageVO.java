package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 入职申请分页 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入职申请分页记录")
public class EntryApplicationPageVO {

    /**
     * 入职申请ID
     */
    @Schema(description = "入职申请ID")
    private Long id;

    /**
     * 候选人姓名
     */
    @Schema(description = "候选人姓名")
    private String candidateName;

    /**
     * 性别
     */
    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCardNo;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID")
    private Long postId;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称")
    private String postName;

    /**
     * 录用类型
     */
    @Schema(description = "录用类型")
    private Integer hireType;

    /**
     * 试用期（月）
     */
    @Schema(description = "试用期（月）")
    private Integer probationMonth;

    /**
     * 试用期薪资比例
     */
    @Schema(description = "试用期薪资比例")
    private BigDecimal probationSalaryRatio;

    /**
     * 预计入职日期
     */
    @Schema(description = "预计入职日期")
    private LocalDate expectedHireDate;

    /**
     * 直接汇报人ID
     */
    @Schema(description = "直接汇报人ID")
    private Long leaderId;

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
     * 审批实例ID
     */
    @Schema(description = "审批实例ID")
    private Long approvalInstanceId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
