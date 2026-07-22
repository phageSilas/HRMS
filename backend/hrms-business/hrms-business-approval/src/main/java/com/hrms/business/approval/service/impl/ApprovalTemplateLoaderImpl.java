package com.hrms.business.approval.service.impl;

import com.hrms.business.approval.config.ApprovalNodeDef;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.ApprovalTemplateLoader;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批模板加载器实现
 * <p>
 * 当前所有审批类型统一为“部门负责人单节点审批”：
 * 入职、转正、调岗、离职、请假、补卡、加班、薪资审批，
 * 均只需要申请人所在部门负责人审批通过即可完成流程。
 * </p>
 */
@Service
public class ApprovalTemplateLoaderImpl implements ApprovalTemplateLoader {

    /**
     * 审批模板定义映射
     * <p>
     * Key 为审批业务类型，Value 为审批节点链。
     * 使用 LinkedHashMap 保证模板插入顺序与迭代顺序一致。
     * </p>
     */
    private static final Map<String, List<ApprovalNodeDef>> TEMPLATES = new LinkedHashMap<>();

    static {
        // 所有审批统一收敛为“部门负责人审批”单节点流程。
        TEMPLATES.put(ApprovalTypeEnum.ENTRY.getCode(), buildDeptHeadOnlyTemplate());
        TEMPLATES.put(ApprovalTypeEnum.REGULAR.getCode(), buildDeptHeadOnlyTemplate());
        TEMPLATES.put(ApprovalTypeEnum.TRANSFER.getCode(), buildDeptHeadOnlyTemplate());
        TEMPLATES.put(ApprovalTypeEnum.LEAVE.getCode(), buildDeptHeadOnlyTemplate());
        TEMPLATES.put(ApprovalTypeEnum.LEAVE_REQUEST.getCode(), buildDeptHeadOnlyTemplate());
        TEMPLATES.put(ApprovalTypeEnum.CORRECTION.getCode(), buildDeptHeadOnlyTemplate());
        TEMPLATES.put(ApprovalTypeEnum.OVERTIME.getCode(), buildDeptHeadOnlyTemplate());
        TEMPLATES.put(ApprovalTypeEnum.SALARY.getCode(), buildDeptHeadOnlyTemplate());
    }

    /**
     * 加载审批模板。
     *
     * @param approvalType 审批类型编码
     * @return 审批节点列表
     */
    @Override
    public List<ApprovalNodeDef> loadTemplate(String approvalType) {
        List<ApprovalNodeDef> nodes = TEMPLATES.get(approvalType);
        if (nodes == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "未找到审批模板：" + approvalType);
        }
        return nodes;
    }

    /**
     * 构建部门负责人单节点审批模板。
     *
     * @return 单节点审批模板
     */
    private static List<ApprovalNodeDef> buildDeptHeadOnlyTemplate() {
        return List.of(new ApprovalNodeDef(
                "DEPT_HEAD",
                "部门负责人审批",
                "DEPT_HEAD",
                1,
                false
        ));
    }
}
