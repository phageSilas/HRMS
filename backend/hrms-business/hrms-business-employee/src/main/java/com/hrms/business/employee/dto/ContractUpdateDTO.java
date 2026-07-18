package com.hrms.business.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新合同 DTO
 */
@Data
@Schema(description = "更新合同请求参数")
public class ContractUpdateDTO {

    @Schema(description = "合同编号", example = "HT-2026-001")
    private String contractNo;

    @Schema(description = "合同类型：1-固定期限 2-无固定期限 3-劳务合同", example = "1")
    private Integer contractType;

    @Schema(description = "合同开始日期", example = "2026-07-15")
    private LocalDate startDate;

    @Schema(description = "合同结束日期", example = "2029-07-14")
    private LocalDate endDate;

    @Schema(description = "试用期（月）", example = "6")
    private Integer probationMonth;

    @Schema(description = "试用期薪资比例（%）", example = "80.00")
    private BigDecimal probationSalaryRatio;

    @Schema(description = "附件文件ID", example = "100")
    private Long attachmentFileId;

    @Schema(description = "备注")
    private String remark;

}
