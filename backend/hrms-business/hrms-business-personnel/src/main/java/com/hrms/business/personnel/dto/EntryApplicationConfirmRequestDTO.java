package com.hrms.business.personnel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 入职确认请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入职确认请求")
public class EntryApplicationConfirmRequestDTO {

    /**
     * 实际入职日期
     */
    @NotNull(message = "实际入职日期不能为空")
    @Schema(description = "实际入职日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate actualHireDate;

}
