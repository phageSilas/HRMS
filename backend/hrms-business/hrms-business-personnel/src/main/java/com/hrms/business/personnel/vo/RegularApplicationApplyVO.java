package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 转正评估发起 VO
 */
@Data
@Schema(description = "转正评估发起结果")
public class RegularApplicationApplyVO {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 审批实例ID
     */
    @Schema(description = "审批实例ID")
    private Long approvalId;

}
