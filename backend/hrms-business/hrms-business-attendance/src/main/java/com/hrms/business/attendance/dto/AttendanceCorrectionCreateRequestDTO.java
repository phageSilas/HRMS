package com.hrms.business.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 补卡申请创建请求。
 */
@Data
public class AttendanceCorrectionCreateRequestDTO {

    /**
     * 补卡日期。
     */
    @NotNull(message = "补卡日期不能为空")
    private LocalDate date;

    /**
     * 补卡原因。
     */
    @NotBlank(message = "补卡原因不能为空")
    private String reason;

    /**
     * 补卡类型，兼容 CLOCK_IN/CLOCK_OUT、in/out、1/2。
     */
    @NotBlank(message = "补卡类型不能为空")
    private String clockType;
}
