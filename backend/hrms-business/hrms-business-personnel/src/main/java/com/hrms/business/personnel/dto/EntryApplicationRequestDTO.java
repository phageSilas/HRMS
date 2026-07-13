package com.hrms.business.personnel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 入职申请请求 DTO
 */
@Data
@Schema(description = "入职申请请求")
public class EntryApplicationRequestDTO {

    @Schema(description = "员工ID")
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @Schema(description = "入职日期")
    private String hireDate;

}
