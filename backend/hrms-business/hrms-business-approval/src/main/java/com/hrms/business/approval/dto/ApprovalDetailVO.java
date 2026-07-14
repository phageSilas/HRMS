package com.hrms.business.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 审批详情
 */
@Data
@Schema(description = "审批详情")
public class ApprovalDetailVO {

    @Schema(description = "审批标题")
    private String title;

    @Schema(description = "业务类型编码")
    private String businessType;

    @Schema(description = "业务类型名称")
    private String businessTypeName;

    @Schema(description = "状态编码")
    private String status;

    @Schema(description = "状态中文名")
    private String statusName;

    @Schema(description = "申请人")
    private String applicantName;

    @Schema(description = "申请时间")
    private String createdAt;

    @Schema(description = "申请内容（按业务类型动态）")
    private Object formData;

    @Schema(description = "审批节点列表")
    private List<ApprovalNodeVO> approvalNodes;

    @Schema(description = "审批历史")
    private List<ApprovalHistoryVO> approvalHistory;

    @Schema(description = "当前用户是否为当前审批人")
    private Boolean currentOperator;

    /**
     * 审批节点（Steps 用）
     */
    @Data
    @Schema(description = "审批节点")
    public static class ApprovalNodeVO {

        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "节点状态：completed / current / pending")
        private String status;

        @Schema(description = "处理人（completed 时有值）")
        private String operatorName;
    }

    /**
     * 审批历史记录（Timeline 用）
     */
    @Data
    @Schema(description = "审批历史记录")
    public static class ApprovalHistoryVO {

        @Schema(description = "处理人")
        private String operatorName;

        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "操作类型：approve / reject / transfer")
        private String action;

        @Schema(description = "操作中文名")
        private String actionName;

        @Schema(description = "审批意见")
        private String comment;

        @Schema(description = "处理时间")
        private String operatedAt;
    }
}
