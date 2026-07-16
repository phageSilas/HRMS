package com.hrms.business.employee.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 员工详情 VO
 */
@Data
@Schema(description = "员工详情")
public class EmployeeDetailVO {

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "关联系统用户ID")
    private Long userId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "性别：1-男 2-女")
    private Integer gender;

    @Schema(description = "性别描述")
    private String genderDesc;

    @Schema(description = "手机号（脱敏）")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "身份证号（脱敏）")
    private String idCardNo;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "户籍地址")
    private String domicileAddress;

    @Schema(description = "现居住地址")
    private String currentAddress;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "职位ID")
    private Long postId;

    @Schema(description = "职位名称")
    private String postName;

    @Schema(description = "职级")
    private String jobLevel;

    @Schema(description = "直接汇报人员工ID")
    private Long leaderId;

    @Schema(description = "直接汇报人姓名")
    private String leaderName;

    @Schema(description = "工作地点")
    private String workLocation;

    @Schema(description = "入职类型：1-全职 2-兼职 3-实习")
    private Integer hireType;

    @Schema(description = "入职类型描述")
    private String hireTypeDesc;

    @Schema(description = "在职状态：1-试用期 2-正式 3-待离职 4-已离职")
    private Integer employmentStatus;

    @Schema(description = "在职状态描述")
    private String employmentStatusDesc;

    @Schema(description = "入职日期")
    private LocalDate hireDate;

    @Schema(description = "试用期（月）")
    private Integer probationMonth;

    @Schema(description = "试用期薪资比例（%）")
    private BigDecimal probationSalaryRatio;

    @Schema(description = "合同类型：1-固定期限 2-无固定期限 3-劳务合同")
    private Integer contractType;

    @Schema(description = "合同类型描述")
    private String contractTypeDesc;

    @Schema(description = "合同到期日")
    private LocalDate contractExpireDate;

    @Schema(description = "薪资账套ID")
    private Long salaryTemplateId;

    @Schema(description = "基本工资")
    private BigDecimal baseSalary;

    @Schema(description = "银行账号（脱敏）")
    private String bankAccount;

    @Schema(description = "开户行")
    private String bankName;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "紧急联系人电话（脱敏）")
    private String emergencyPhone;

    @Schema(description = "字段权限配置")
    private Map<String, List<String>> fieldPermissions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
