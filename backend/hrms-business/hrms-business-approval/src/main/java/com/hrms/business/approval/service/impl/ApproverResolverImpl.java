package com.hrms.business.approval.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.approval.entity.ApprovalDelegationEntity;
import com.hrms.business.approval.mapper.ApprovalDelegationMapper;
import com.hrms.business.approval.service.ApproverResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审批人解析器实现
 * <p>
 * TODO: 当前 `resolveApprover` 返回配置的默认审批人ID。
 * 待组织架构模块完成后，替换为真实的部门负责人/上级/角色查询逻辑。
 * </p>
 */
@Service
public class ApproverResolverImpl implements ApproverResolver {

    @Autowired
    private ApprovalDelegationMapper delegationMapper;

    /**
     * 默认审批人ID（开发测试用，后续对接组织架构模块后移除）
     */
    @Value("${approval.default-approver-id:1}")
    private Long defaultApproverId;

    @Override
    public Long resolveApprover(String approverType, Long applicantDeptId, Long bizId) {
        // TODO: 对接组织架构模块后替换为真实查询
        // switch (approverType) {
        //     case "DEPT_HEAD":
        //         // 部门负责人：查询 applicantDeptId 的部门负责人
        //         // DeptEntity dept = deptService.getById(applicantDeptId);
        //         // return dept != null ? dept.getHeadUserId() : null;
        //
        //     case "SUPERIOR_DEPT_HEAD":
        //         // 直接上级：查询上一级部门的负责人
        //         // DeptEntity dept = deptService.getById(applicantDeptId);
        //         // if (dept != null && dept.getParentId() != null) {
        //         //     DeptEntity parentDept = deptService.getById(dept.getParentId());
        //         //     return parentDept != null ? parentDept.getHeadUserId() : null;
        //         // }
        //         // return null;
        //
        //     case "HR_HEAD":
        //         // HR负责人：通过角色查询
        //         // return userService.getUserIdByRole("HR_HEAD");
        //
        //     case "FINANCE_HEAD":
        //         // 财务负责人：通过角色查询
        //         // return userService.getUserIdByRole("FINANCE_HEAD");
        //
        //     case "BOSS":
        //         // 老板：查询最高级别审批人
        //         // return userService.getUserIdByRole("BOSS");
        // }
        return defaultApproverId;
    }

    @Override
    public Long checkDelegation(Long approverUserId) {
        if (approverUserId == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        ApprovalDelegationEntity delegation = delegationMapper.selectOne(
                Wrappers.lambdaQuery(ApprovalDelegationEntity.class)
                        .eq(ApprovalDelegationEntity::getDelegatorId, approverUserId)
                        .eq(ApprovalDelegationEntity::getStatus, 1)
                        .le(ApprovalDelegationEntity::getStartDate, now)
                        .ge(ApprovalDelegationEntity::getEndDate, now)
                        .last("LIMIT 1")
        );

        return delegation != null ? delegation.getDelegateToId() : null;
    }
}
