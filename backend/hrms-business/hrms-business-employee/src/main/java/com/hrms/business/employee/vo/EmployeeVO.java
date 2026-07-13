package com.hrms.business.employee.vo;

import com.hrms.common.enums.EmployeeStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工 VO
 */
@Data
@Schema(description = "员工")
public class EmployeeVO {

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "性别：1-男，2-女，0-未知")
    private Integer gender;

    @Schema(description = "出生日期")
    private String birthday;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "职位ID")
    private Long postId;

    @Schema(description = "职位名称")
    private String postName;

    @Schema(description = "入职日期")
    private String hireDate;

    @Schema(description = "状态：1-试用期，2-正式，3-离职")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "工作地点")
    private String workLocation;

    @Schema(description = "最高学历")
    private String education;

    @Schema(description = "合同开始日期")
    private String contractStartDate;

    @Schema(description = "合同结束日期")
    private String contractEndDate;

    @Schema(description = "创建时间")
    private Long createTime;

}
