package com.hrms.business.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 待办列表条目
 */
@Data
@Schema(description = "待办任务视图")
public class PendingTaskVO {

    @Schema(description = "审批实例ID")
    private Long id;

    @Schema(description = "审批任务ID（操作时使用）")
    private Long taskId;

    @Schema(description = "业务类型编码")
    private String businessType;

    @Schema(description = "业务类型名称")
    private String businessTypeName;

    @Schema(description = "审批标题")
    private String title;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "当前审批节点名称")
    private String nodeName;

    @Schema(description = "是否代审")
    private Boolean delegateFlag;

    @Schema(description = "代审标记文案")
    private String delegateMark;

    @Schema(description = "申请时间")
    private String createdAt;

    @Schema(description = "截止时间")
    private String deadline;

    @Schema(description = "状态编码")
    private String status;

    @Schema(description = "状态中文名")
    private String statusName;
}
