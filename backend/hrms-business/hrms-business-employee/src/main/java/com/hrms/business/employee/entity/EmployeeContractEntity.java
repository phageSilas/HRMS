package com.hrms.business.employee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工合同实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee_contract")
public class EmployeeContractEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 合同类型：1-固定期限 2-无固定期限 3-劳务合同
     */
    private Integer contractType;

    /**
     * 合同开始日期
     */
    private LocalDate startDate;

    /**
     * 合同结束日期
     */
    private LocalDate endDate;

    /**
     * 试用期（月）
     */
    private Integer probationMonth;

    /**
     * 试用期薪资比例（%）
     */
    private BigDecimal probationSalaryRatio;

    /**
     * 附件文件ID
     */
    private Long attachmentFileId;

    /**
     * 续签次数
     */
    private Integer signingCount;

    /**
     * 备注
     */
    private String remark;

}
