package com.hrms.business.attendance.vo;

import lombok.Data;

/**
 * 请假类型视图。
 */
@Data
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
