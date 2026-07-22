package com.hrms.business.approval.service.impl;

import com.hrms.business.approval.config.ApprovalNodeDef;
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
 * 审批流定义（系分文档 §3.7.3）：
 * ┌───────────────┬──────────┬──────────────────────────────────────┐
 * │ 业务类型       │ 申请者   │ 审批节点链                           │
 * ├───────────────┼──────────┼──────────────────────────────────────┤
 * │ ENTRY         │ HR       │ 部门负责人 → [HR负责人可选]          │
 * │ REGULAR       │ HR       │ 部门负责人 → HR负责人                │
 * │ TRANSFER      │ HR       │ 原部门负责人 → 新部门负责人 → HR负责人 │
 * │ LEAVE         │ HR       │ 部门负责人 → HR负责人                │
 * │ LEAVE_REQUEST │ 员工     │ 上级部门负责人（无上级则提示"无上级审批人"）│
 * │ CORRECTION    │ 员工     │ 上级部门负责人（无上级则提示"无上级审批人"）│
 * │ OVERTIME      │ 员工     │ 上级部门负责人（无上级则提示"无上级审批人"）│
 * │ SALARY        │ HR       │ 财务专员 → [老板可选]                │
 * └───────────────┴──────────┴──────────────────────────────────────┘
 * </p>
 */
@Service
public class ApprovalTemplateLoaderImpl implements ApprovalTemplateLoader {

    /**
     * 审批模板定义映射
     * <p>
     * Key 为审批业务类型（如 ENTRY、LEAVE），Value 为审批节点链。
     * 使用 LinkedHashMap 保证模板插入顺序与迭代顺序一致。
     * </p>
     */
    private static final Map<String, List<ApprovalNodeDef>> TEMPLATES = new LinkedHashMap<>();

    static {
        // ENTRY 入职审批：部门负责人 → [HR负责人可选]
        TEMPLATES.put("ENTRY", List.of(
                new ApprovalNodeDef("DEPT_HEAD", "部门负责人审批", "DEPT_HEAD", 1, false),
                new ApprovalNodeDef("HR_HEAD", "HR负责人审批", "HR_HEAD", 2, true)
        ));

        // REGULAR 转正审批：部门负责人 → HR负责人
        TEMPLATES.put("REGULAR", List.of(
                new ApprovalNodeDef("DEPT_HEAD", "部门负责人审批", "DEPT_HEAD", 1, false),
                new ApprovalNodeDef("HR_HEAD", "HR负责人审批", "HR_HEAD", 2, false)
        ));

        // TRANSFER 调岗审批：原部门负责人 → 新部门负责人 → HR负责人
        TEMPLATES.put("TRANSFER", List.of(
                new ApprovalNodeDef("ORIGIN_DEPT_HEAD", "原部门负责人审批", "DEPT_HEAD", 1, false),
                new ApprovalNodeDef("NEW_DEPT_HEAD", "新部门负责人审批", "DEPT_HEAD", 2, false),
                new ApprovalNodeDef("HR_HEAD", "HR负责人审批", "HR_HEAD", 3, false)
        ));
        // 注：TRANSFER 中两个节点都是 DEPT_HEAD 类型，但分别对应调出/调入部门
        // 需在调用 startApproval 时通过 formData 传入 origDeptId / newDeptId

        // LEAVE 离职审批：部门负责人 → HR负责人
        TEMPLATES.put("LEAVE", List.of(
                new ApprovalNodeDef("DEPT_HEAD", "部门负责人审批", "DEPT_HEAD", 1, false),
                new ApprovalNodeDef("HR_HEAD", "HR负责人审批", "HR_HEAD", 2, false)
        ));

        // LEAVE_REQUEST 请假审批：上级部门负责人审批
        TEMPLATES.put("LEAVE_REQUEST", List.of(
                new ApprovalNodeDef("PARENT_DEPT_HEAD", "上级部门负责人审批", "PARENT_DEPT_HEAD", 1, false)
        ));

        // CORRECTION 补卡审批：上级部门负责人
        TEMPLATES.put("CORRECTION", List.of(
                new ApprovalNodeDef("PARENT_DEPT_HEAD", "上级部门负责人审批", "PARENT_DEPT_HEAD", 1, false)
        ));

        // OVERTIME 加班审批：上级部门负责人
        TEMPLATES.put("OVERTIME", List.of(
                new ApprovalNodeDef("PARENT_DEPT_HEAD", "上级部门负责人审批", "PARENT_DEPT_HEAD", 1, false)
        ));

        // SALARY 薪资批次审批：财务专员 → [老板可选]
        TEMPLATES.put("SALARY", List.of(
                new ApprovalNodeDef("FINANCE", "财务审批", "FINANCE_HEAD", 1, false),
                new ApprovalNodeDef("BOSS", "老板审批", "BOSS", 2, true)
        ));
    }

    @Override
    public List<ApprovalNodeDef> loadTemplate(String approvalType) {
        List<ApprovalNodeDef> nodes = TEMPLATES.get(approvalType);
        if (nodes == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "未找到审批模板：" + approvalType);
        }
        return nodes;
    }
}
