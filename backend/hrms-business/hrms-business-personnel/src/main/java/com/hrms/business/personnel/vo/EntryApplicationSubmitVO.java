package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 入职申请提交审批 VO
 */
@Data
@Schema(description = "入职申请提交审批结果")
public class EntryApplicationSubmitVO {

    /**
     * 审批实例ID
     */
    @Schema(description = "审批实例ID")
    private Long approvalInstanceId;

    /**
     * 审批状态
     */
    @Schema(description = "审批状态")
    private Integer approvalStatus;

}
