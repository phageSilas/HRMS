package com.hrms.business.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 审批操作结果
 */
@Data
@Schema(description = "审批操作结果")
public class OperateResultVO {

    @Schema(description = "操作是否成功")
    private Boolean success;

    public static OperateResultVO ok() {
        OperateResultVO vo = new OperateResultVO();
        vo.setSuccess(true);
        return vo;
    }
}
