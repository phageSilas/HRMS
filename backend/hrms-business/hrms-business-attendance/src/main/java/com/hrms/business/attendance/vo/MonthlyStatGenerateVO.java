package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月度考勤统计生成结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatGenerateVO {

    /**
     * 统计月份。
     */
    private String month;

    /**
     * 触发员工数量。
     */
    private Integer employeeCount;

    /**
     * 是否触发成功。
     */
    private Boolean success;
}
