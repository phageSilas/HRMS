package com.hrms.business.approval.service.impl;

import com.hrms.business.approval.config.ApprovalNodeDef;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 审批模板加载器测试
 */
class ApprovalTemplateLoaderImplTest {

    private final ApprovalTemplateLoaderImpl templateLoader = new ApprovalTemplateLoaderImpl();

    /**
     * 所有审批类型都应收敛为部门负责人单节点审批。
     */
    @Test
    void shouldLoadDeptHeadOnlyTemplateForAllApprovalTypes() {
        for (ApprovalTypeEnum approvalType : ApprovalTypeEnum.values()) {
            List<ApprovalNodeDef> nodes = templateLoader.loadTemplate(approvalType.getCode());
            assertEquals(1, nodes.size(), approvalType.getCode() + " 应仅保留一个审批节点");

            ApprovalNodeDef node = nodes.get(0);
            assertEquals("DEPT_HEAD", node.getNodeCode());
            assertEquals("部门负责人审批", node.getNodeName());
            assertEquals("DEPT_HEAD", node.getApproverType());
            assertEquals(1, node.getSortNo());
            assertFalse(node.isOptional());
        }
    }
}
