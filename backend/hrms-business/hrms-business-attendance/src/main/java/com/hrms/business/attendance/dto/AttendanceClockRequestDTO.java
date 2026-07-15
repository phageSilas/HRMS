package com.hrms.business.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 员工打卡请求参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceClockRequestDTO {

    /**
     * 打卡类型，兼容 CLOCK_IN/CLOCK_OUT、in/out、1/2；为空时后端自动判断。
     */
    private String type;

    /**
     * 纬度。
     */
    private BigDecimal latitude;

    /**
     * 经度。
     */
    private BigDecimal longitude;

    /**
     * 设备信息。
     */
    private String deviceInfo;
}
