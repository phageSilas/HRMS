package com.hrms.business.employee.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee")
public class EmployeeEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 关联系统用户ID
     */
    private Long userId;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 职位ID
     */
    private Long postId;

    /**
     * 直接汇报人员工ID
     */
    private Long leaderId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 性别：1-男 2-女
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
     * 身份证号（AES-256 GCM 加密存储）
     */
    private String idCardNo;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 户籍地址
     */
    private String domicileAddress;

    /**
     * 现居住地址
     */
    private String currentAddress;

    /**
     * 职级
     */
    private String jobLevel;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 入职类型：1-全职 2-兼职 3-实习
     */
    private Integer hireType;

    /**
     * 在职状态：1-试用期 2-正式 3-待离职 4-已离职
     */
    private Integer employmentStatus;

    /**
     * 入职日期
     */
    private LocalDate hireDate;

    /**
     * 试用期（月）
     */
    private Integer probationMonth;

    /**
     * 试用期薪资比例（%）
     */
    private BigDecimal probationSalaryRatio;

    /**
     * 合同类型：1-固定期限 2-无固定期限 3-劳务合同
     */
    private Integer contractType;

    /**
     * 合同到期日
     */
    private LocalDate contractExpireDate;

    /**
     * 薪资账套ID
     */
    private Long salaryTemplateId;

    /**
     * 基本工资
     */
    private BigDecimal baseSalary;

    /**
     * 银行账号（AES-256 GCM 加密存储）
     */
    private String bankAccount;

    /**
     * 开户行
     */
    private String bankName;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    private String emergencyPhone;

    /**
     * 备注
     */
    private String remark;

}
