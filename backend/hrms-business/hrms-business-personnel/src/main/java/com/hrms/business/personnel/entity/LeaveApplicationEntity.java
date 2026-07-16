package com.hrms.business.personnel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDate;

/**
 * 离职申请实体
 */
@Data
@TableName("hr_leave_application")
public class LeaveApplicationEntity extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 离职类型：1-主动辞职，2-被动辞退，3-合同到期不续签，4-其他
     */
    private Integer leaveType;

    /**
     * 离职原因
     */
    private String leaveReason;

    /**
     * 申请日期
     */
    private LocalDate applyDate;

    /**
     * 预计最后工作日
     */
    private LocalDate expectedLastWorkDate;

    /**
     * 实际最后工作日
     */
    private LocalDate lastWorkDate;

    /**
     * 交接人员工ID
     */
    private Long handoverEmployeeId;

    /**
     * 交接状态：0-未交接，1-交接中，2-已交接
     */
    private Integer handoverStatus;

    /**
     * 交接说明
     */
    private String handoverNote;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态：0-草稿，1-审批中，2-已通过，3-已拒绝
     */
    private Integer approvalStatus;

    /**
     * 备注
     */
    private String remark;

}
