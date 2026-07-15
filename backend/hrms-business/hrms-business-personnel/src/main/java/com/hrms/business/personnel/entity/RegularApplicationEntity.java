package com.hrms.business.personnel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 转正申请实体
 */
@Data
@TableName("hr_regular_application")
public class RegularApplicationEntity extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 试用期开始日期
     */
    private LocalDate probationStartDate;

    /**
     * 试用期结束日期
     */
    private LocalDate probationEndDate;

    /**
     * 评估结果：1-转正，2-延长试用，3-辞退
     */
    private Integer evaluateResult;

    /**
     * 延长试用月数
     */
    private Integer extendMonth;

    /**
     * 调薪金额
     */
    private BigDecimal salaryAdjustment;

    /**
     * 评估意见
     */
    private String evaluateOpinion;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态：0-草稿，1-审批中，2-已通过，3-已拒绝
     */
    private Integer approvalStatus;

    /**
     * 实际转正日期
     */
    private LocalDate regularDate;

    /**
     * 备注
     */
    private String remark;

}
