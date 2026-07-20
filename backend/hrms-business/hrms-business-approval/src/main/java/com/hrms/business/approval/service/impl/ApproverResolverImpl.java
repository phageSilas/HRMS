package com.hrms.business.approval.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.approval.dto.EmployeeBriefDTO;
import com.hrms.business.approval.entity.ApprovalDelegationEntity;
import com.hrms.business.approval.mapper.ApprovalDelegationMapper;
import com.hrms.business.approval.mapper.ApprovalEmployeeMapper;
import com.hrms.business.approval.service.ApproverResolver;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.entity.UserRoleEntity;
import com.hrms.system.auth.mapper.RoleMapper;
import com.hrms.system.auth.mapper.UserMapper;
import com.hrms.system.auth.mapper.UserRoleMapper;
import com.hrms.system.organization.entity.DeptEntity;
import com.hrms.system.organization.mapper.DeptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批人解析器实现
 * <p>
 * 根据审批人类型和业务上下文，从组织架构/角色中解析真实的审批人用户ID。
 * 支持：部门负责人(DEPT_HEAD)、直接上级(SUPERIOR_DEPT_HEAD)、
 * HR负责人(HR_HEAD)、财务负责人(FINANCE_HEAD)、老板(BOSS)。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApproverResolverImpl implements ApproverResolver {

    private final ApprovalDelegationMapper delegationMapper;
    private final ApprovalEmployeeMapper approvalEmployeeMapper;
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 解析审批人
     * <p>
     * 根据审批人类型从组织架构或角色中查找对应的用户 ID。
     * 支持类型：DEPT_HEAD（部门负责人）、SUPERIOR_DEPT_HEAD（直接上级）、
     * HR_HEAD（HR 负责人）、FINANCE_HEAD（财务负责人）、BOSS（老板）。
     * </p>
     *
     * @param approverType    审批人类型编码
     * @param applicantDeptId 申请人部门 ID（DEPT_HEAD 类型时需要）
     * @param bizId           业务主键 ID（SUPERIOR_DEPT_HEAD 类型时需要）
     * @return 审批人用户 ID，无法解析时返回 null
     */
    @Override
    public Long resolveApprover(String approverType, Long applicantDeptId, Long bizId) {
        Long userId = switch (approverType) {
            case "DEPT_HEAD" -> resolveDeptHead(applicantDeptId);
            case "SUPERIOR_DEPT_HEAD" -> resolveSuperiorDeptHead(bizId);
            case "HR_HEAD" -> resolveRoleUser("HR_HEAD");
            case "FINANCE_HEAD" -> resolveRoleUser("FINANCE_HEAD");
            case "BOSS" -> resolveRoleUser("BOSS");
            default -> {
                log.warn("未知的审批人类型: {}", approverType);
                yield null;
            }
        };

        if (userId == null) {
            log.warn("无法解析审批人: approverType={}, applicantDeptId={}, bizId={}",
                    approverType, applicantDeptId, bizId);
        }
        return userId;
    }

    /**
     * 解析部门负责人：查询 sys_dept 的 leader_user_id
     */
    private Long resolveDeptHead(Long deptId) {
        if (deptId == null) {
            log.warn("解析部门负责人失败: deptId 为空");
            return null;
        }
        DeptEntity dept = deptMapper.selectById(deptId);
        if (dept == null) {
            log.warn("解析部门负责人失败: 部门不存在, deptId={}", deptId);
            return null;
        }
        return dept.getLeaderUserId();
    }

    /**
     * 解析直接上级：通过 hr_employee.leader_id 链路查询
     * 先根据申请人 userId 查员工，再查其 leader 的 userId
     * <p>
     * 若员工未配置直接上级（leader_id 为空），则降级为该员工所在部门的部门负责人。
     * 这样即使组织架构数据不完整，补卡/加班等审批也能正常流转。
     * </p>
     */
    private Long resolveSuperiorDeptHead(Long bizId) {
        Long currentUserId = com.hrms.common.security.SecurityContextHolder.getUserId();
        if (currentUserId == null) {
            log.warn("解析直接上级失败: 无法获取当前用户ID");
            return null;
        }

        // 1. 先尝试通过 leader_id 链路查找直接上级
        Long leaderUserId = findLeaderByUserId(currentUserId);
        if (leaderUserId != null) {
            return leaderUserId;
        }

        // 2. 直接上级不存在 → 降级为部门负责人审批
        log.warn("员工无直接上级配置，降级为部门负责人审批, userId={}", currentUserId);
        EmployeeBriefDTO emp = approvalEmployeeMapper.findByUserId(currentUserId);
        if (emp != null && emp.getDeptId() != null) {
            Long deptHeadUserId = resolveDeptHead(emp.getDeptId());
            if (deptHeadUserId != null) {
                log.info("降级为部门负责人审批成功: userId={}, deptId={}, deptHeadUserId={}",
                        currentUserId, emp.getDeptId(), deptHeadUserId);
                return deptHeadUserId;
            }
            log.warn("降级为部门负责人失败: 部门未设置负责人, deptId={}", emp.getDeptId());
        }

        // 3. 部门负责人也未配置 → 兜底为 HR 负责人
        log.warn("降级为HR负责人审批, userId={}", currentUserId);
        Long hrHeadUserId = resolveRoleUser("HR_HEAD");
        if (hrHeadUserId != null) {
            log.info("降级为HR负责人审批成功: userId={}, hrHeadUserId={}", currentUserId, hrHeadUserId);
            return hrHeadUserId;
        }

        return null;
    }

    /**
     * 根据用户 ID 查找其直接上级的用户 ID
     * <p>
     * 链路：userId → hr_employee.leader_id（员工 ID）→ 上级的 hr_employee.user_id
     * </p>
     *
     * @param userId 当前用户 ID
     * @return 直接上级的用户 ID，未配置时返回 null
     */
    private Long findLeaderByUserId(Long userId) {
        // 1. 根据 userId 查员工记录，获取 leaderId（员工ID）
        EmployeeBriefDTO emp = approvalEmployeeMapper.findByUserId(userId);
        if (emp == null || emp.getLeaderId() == null) {
            log.warn("查找直接上级失败: 员工不存在或未设置上级, userId={}", userId);
            return null;
        }

        // 2. 根据 leaderId（员工ID）查上级的员工记录，获取其 userId
        EmployeeBriefDTO leader = approvalEmployeeMapper.findById(emp.getLeaderId());
        if (leader == null) {
            log.warn("查找直接上级失败: 上级员工记录不存在, leaderEmployeeId={}", emp.getLeaderId());
            return null;
        }

        return leader.getUserId();
    }

    /**
     * 根据角色编码查询有该角色的第一个用户
     */
    private Long resolveRoleUser(String roleCode) {
        // 1. 查询角色
        RoleEntity role = roleMapper.selectOne(
                Wrappers.lambdaQuery(RoleEntity.class)
                        .eq(RoleEntity::getRoleCode, roleCode)
                        .eq(RoleEntity::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (role == null) {
            log.warn("解析角色用户失败: 角色不存在或已禁用, roleCode={}", roleCode);
            return null;
        }

        // 2. 查询该角色的第一个关联用户
        UserRoleEntity userRole = userRoleMapper.selectOne(
                Wrappers.lambdaQuery(UserRoleEntity.class)
                        .eq(UserRoleEntity::getRoleId, role.getId())
                        .last("LIMIT 1")
        );
        if (userRole == null) {
            log.warn("解析角色用户失败: 角色下无用户, roleCode={}, roleId={}", roleCode, role.getId());
            return null;
        }

        return userRole.getUserId();
    }

    /**
     * 检查审批人是否存在生效的委托关系
     * <p>
     * 查询委托表中当前时间在有效期内的生效委托记录，
     * 若存在则返回被委托人 ID，后续由被委托人代为审批。
     * </p>
     *
     * @param approverUserId 原审批人用户 ID
     * @return 被委托人用户 ID，无委托时返回 null
     */
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
