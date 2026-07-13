package com.hrms.business.employee.entity;

import com.hrms.common.entity.BaseEntity;
import com.hrms.common.enums.EmployeeStatusEnum;
import lombok.Data;

/**
 * 员工实体
 */
@Data
public class EmployeeEntity extends BaseEntity {

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别：1-男，2-女，0-未知
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 身份证号（加密）
     */
    private String idCardNo;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 职位ID
     */
    private Long postId;

    /**
     * 入职日期
     */
    private String hireDate;

    /**
     * 状态：1-试用期，2-正式，3-离职
     */
    private Integer status;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 最高学历
     */
    private String education;

    /**
     * 专业
     */
    private String major;

    /**
     * 毕业院校
     */
    private String school;

    /**
     * 合同开始日期
     */
    private String contractStartDate;

    /**
     * 合同结束日期
     */
    private String contractEndDate;

}
