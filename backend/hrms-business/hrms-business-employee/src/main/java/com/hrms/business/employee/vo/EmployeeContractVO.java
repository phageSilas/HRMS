package com.hrms.business.employee.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工合同 VO
 */
@Data
@Schema(description = "员工合同信息")
public class EmployeeContractVO {

    @Schema(description = "合同ID")
    private Long id;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "合同类型：1-固定期限 2-无固定期限 3-劳务合同")
    private Integer contractType;

    @Schema(description = "合同类型描述")
    private String contractTypeDesc;

    @Schema(description = "合同开始日期")
    private LocalDate startDate;

    @Schema(description = "合同结束日期")
    private LocalDate endDate;

    @Schema(description = "试用期（月）")
    private Integer probationMonth;

    @Schema(description = "试用期薪资比例（%）")
    private java.math.BigDecimal probationSalaryRatio;

    @Schema(description = "附件文件ID")
    private Long attachmentFileId;

    @Schema(description = "续签次数")
    private Integer signingCount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
