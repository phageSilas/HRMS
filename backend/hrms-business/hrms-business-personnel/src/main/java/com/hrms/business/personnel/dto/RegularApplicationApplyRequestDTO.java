package com.hrms.business.personnel.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 转正评估发起请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "转正评估发起请求")
public class RegularApplicationApplyRequestDTO {

    /**
     * 表现评价
     */
    @NotBlank(message = "表现评价不能为空")
    @JsonAlias("evaluation")
    @Schema(description = "表现评价", requiredMode = Schema.RequiredMode.REQUIRED)
    private String evaluateOpinion;

    /**
     * 评估结果：pass / extend / terminate
     */
    @NotBlank(message = "评估结果不能为空")
    @Pattern(regexp = "^(pass|extend|terminate)$", message = "评估结果取值不正确")
    @Schema(description = "评估结果：pass / extend / terminate", requiredMode = Schema.RequiredMode.REQUIRED)
    private String result;

    /**
     * 转正后薪资调整
     */
    @DecimalMin(value = "0.00", message = "转正后薪资调整不能小于0")
    @JsonAlias("newSalary")
    @Schema(description = "转正后薪资调整")
    private BigDecimal salaryAdjustment;

    /**
     * 延长试用月数
     */
    @Min(value = 1, message = "延长试用月数必须大于0")
    @JsonAlias("extendMonths")
    @Schema(description = "延长试用月数")
    private Integer extendMonth;

}
