package com.hrms.business.approval.config;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 审批节点定义
 * <p>
 * 描述审批流程中的一个节点，包括节点名称、审批人类型、是否可选等。
 * 可选节点（如 [HR负责人]）在无审批人时可跳过。
 * </p>
 */
@Data
@AllArgsConstructor
public class ApprovalNodeDef {

    /**
     * 节点编码
     */
    private String nodeCode;

    /**
     * 节点名称（如"部门负责人审批"）
     */
    private String nodeName;

    /**
     * 审批人类型：DEPT_HEAD（部门负责人）/ SUPERIOR_DEPT_HEAD（上级部门负责人）/ HR_HEAD（HR负责人）/ FINANCE_HEAD（财务负责人）
     */
    private String approverType;

    /**
     * 节点顺序
     */
    private Integer sortNo;

    /**
     * 是否可选：true-无审批人时可跳过 / false-必须有审批人
     */
    private boolean optional;
}
