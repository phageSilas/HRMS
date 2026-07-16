package com.hrms.business.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请假类型视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeVO {

    /**
     * 字典ID。
     */
    private Long id;

    /**
     * 类型名称。
     */
    private String label;

    /**
     * 类型值。
     */
    private String value;
}
