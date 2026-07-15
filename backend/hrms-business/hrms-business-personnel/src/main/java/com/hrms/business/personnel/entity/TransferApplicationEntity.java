package com.hrms.business.personnel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 调岗申请实体
 */
@Data
@TableName("hr_transfer_application")
public class TransferApplicationEntity extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 原部门ID
     */
    private Long fromDeptId;

    /**
     * 新部门ID
     */
    private Long toDeptId;

    /**
     * 原岗位ID
     */
    private Long fromPostId;

    /**
     * 新岗位ID
     */
    private Long toPostId;

    /**
     * 原职级
     */
    private String fromJobLevel;

    /**
     * 新职级
     */
    private String toJobLevel;

    /**
     * 原汇报人ID
     */
    private Long fromLeaderId;

    /**
     * 新汇报人ID
     */
    private Long toLeaderId;

    /**
     * 薪资调整金额
     */
    private BigDecimal salaryAdjustment;

    /**
     * 生效日期
     */
    private LocalDate effectiveDate;

    /**
     * 调岗原因
     */
    private String reason;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态
     */
    private Integer approvalStatus;

}
