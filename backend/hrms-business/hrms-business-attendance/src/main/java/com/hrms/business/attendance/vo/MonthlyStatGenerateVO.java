package com.hrms.business.attendance.vo;

import lombok.Data;

/**
 * 月度考勤统计生成结果。
 */
@Data
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
