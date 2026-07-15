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

    @Schema(description = "当前任务状态：PROCESSED / TRANSFERRED")
    private String taskStatus;

    @Schema(description = "审批实例状态：PENDING / APPROVED / REJECTED")
    private String instanceStatus;

    @Schema(description = "下一节点名称（null 表示审批已结束）")
    private String nextNodeName;

    public static OperateResultVO ok() {
        OperateResultVO vo = new OperateResultVO();
        vo.setSuccess(true);
        return vo;
    }
}
