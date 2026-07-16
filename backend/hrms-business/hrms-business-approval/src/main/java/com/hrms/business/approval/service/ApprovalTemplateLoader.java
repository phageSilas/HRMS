package com.hrms.business.approval.service;

import com.hrms.business.approval.config.ApprovalNodeDef;

import java.util.List;

/**
 * 审批模板加载器
 * <p>
 * 根据审批类型加载对应的审批节点链定义。
 * </p>
 */
public interface ApprovalTemplateLoader {

    /**
     * 加载审批模板
     *
     * @param approvalType 审批类型编码（如 ENTRY, REGULAR 等）
     * @return 审批节点列表
     * @throws com.hrms.common.exception.GlobalException 未找到模板时抛出 NOT_FOUND
     */
    List<ApprovalNodeDef> loadTemplate(String approvalType);
}
