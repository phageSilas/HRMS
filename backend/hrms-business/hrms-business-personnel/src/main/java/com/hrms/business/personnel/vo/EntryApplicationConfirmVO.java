package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 入职确认 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入职确认结果")
public class EntryApplicationConfirmVO {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID")
    private Long employeeId;

    /**
     * 员工工号
     */
    @Schema(description = "员工工号")
    private String employeeNo;

}
