package com.hrms.business.personnel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 入职申请实体
 */
@Data
@TableName("hr_entry_application")
public class EntryApplicationEntity extends BaseEntity {

    /**
     * 候选人姓名
     */
    private String candidateName;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 拟入职部门ID
     */
    private Long deptId;

    /**
     * 拟入职岗位ID
     */
    private Long postId;

    /**
     * 录用类型：1-全职，2-兼职，3-实习
     */
    private Integer hireType;

    /**
     * 试用期（月）
     */
    private Integer probationMonth;

    /**
     * 试用期薪资比例（%）
     */
    private BigDecimal probationSalaryRatio;

    /**
     * 预计入职日期
     */
    private LocalDate expectedHireDate;

    /**
     * 直接汇报人ID
     */
    private Long leaderId;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态：0-草稿，1-审批中，2-已通过，3-已拒绝，5-已入职
     */
    private Integer approvalStatus;

    /**
     * 实际入职日期
     */
    private LocalDate actualHireDate;

}
