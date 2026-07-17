package com.hrms.business.salary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 薪资批次人工调整请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryBatchAdjustmentRequestDTO {

    /**
     * 被调整员工ID。
     */
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    /**
     * 人工调整项目列表。
     */
    @Valid
    @NotEmpty(message = "人工调整项目不能为空")
    private List<AdjustmentItem> adjustments;

    /**
     * 人工调整项目。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdjustmentItem {

        /**
         * 薪资项目编码。
         */
        @NotBlank(message = "薪资项目编码不能为空")
        private String itemCode;

        /**
         * 调整金额，正数增加、负数减少。
         */
        @NotNull(message = "调整金额不能为空")
        private BigDecimal adjustAmount;

        /**
         * 调整原因。
         */
        @NotBlank(message = "调整原因不能为空")
        private String reason;
    }
}
