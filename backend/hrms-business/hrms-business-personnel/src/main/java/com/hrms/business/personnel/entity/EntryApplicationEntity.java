package com.hrms.business.personnel.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

/**
 * 入职申请实体
 */
@Data
public class EntryApplicationEntity extends BaseEntity {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 入职日期
     */
    private String hireDate;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

}
