package com.hrms.business.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 月度考勤统计生成请求。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatGenerateRequestDTO {

    /**
     * 统计月份，格式 yyyy-MM。
     */
    @NotBlank(message = "统计月份不能为空")
    private String month;

    /**
     * 员工ID列表；为空时统计全部员工。
     */
    private List<Long> employeeIds;
}
