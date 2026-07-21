package com.hrms.business.approval.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.approval.dto.DelegationCreateRequest;
import com.hrms.business.approval.dto.DelegationListVO;
import com.hrms.business.approval.dto.DelegationVO;
import com.hrms.business.approval.dto.EmployeeBriefDTO;
import com.hrms.business.approval.entity.ApprovalDelegationEntity;
import com.hrms.business.approval.mapper.ApprovalDelegationMapper;
import com.hrms.business.approval.mapper.ApprovalEmployeeMapper;
import com.hrms.business.approval.service.DelegationService;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.system.auth.entity.UserEntity;
import com.hrms.system.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 委托审批服务实现
 * <p>
 * 提供委托创建、取消、查询功能。委托期间，原审批人的待办任务由被委托人代为处理。
 * 创建委托时校验有效期重叠，同一委托人不可有重叠的生效委托。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DelegationServiceImpl implements DelegationService {

    /** 日期时间格式化器：yyyy-MM-dd HH:mm:ss */
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApprovalDelegationMapper delegationMapper;
    private final ApprovalEmployeeMapper approvalEmployeeMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDelegation(Long userId, DelegationCreateRequest request) {
        LocalDateTime startDate = LocalDateTime.parse(request.getStartTime(), DTF);
        LocalDateTime endDate = LocalDateTime.parse(request.getEndTime(), DTF);

        // 校验：结束时间不能早于开始时间
        if (endDate.isBefore(startDate)) {
            throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "结束时间不能早于开始时间");
        }
        // 校验：结束时间不能早于当前时间
        if (endDate.isBefore(LocalDateTime.now())) {
            throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "结束时间不能早于当前时间");
        }

        // 有效期重叠检测：同一委托人不可有重叠的生效委托
        Long overlapCount = delegationMapper.selectCount(
                Wrappers.lambdaQuery(ApprovalDelegationEntity.class)
                        .eq(ApprovalDelegationEntity::getDelegatorId, userId)
                        .eq(ApprovalDelegationEntity::getStatus, 1)
                        .lt(ApprovalDelegationEntity::getStartDate, endDate)
                        .gt(ApprovalDelegationEntity::getEndDate, startDate)
        );
        if (overlapCount > 0) {
            throw new GlobalException(ErrorCode.DATA_DUPLICATE, "存在重叠的生效委托，请调整时间后重试");
        }

        // 创建委托：将员工ID转换为系统用户ID
        EmployeeBriefDTO emp = approvalEmployeeMapper.findById(request.getDelegateeId());
        if (emp == null || emp.getUserId() == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "被委托人不存在或未关联系统用户");
        }
        ApprovalDelegationEntity entity = new ApprovalDelegationEntity();
        entity.setDelegatorId(userId);
        entity.setDelegatorName(getUserName(userId));
        entity.setDelegateToId(emp.getUserId());
        entity.setDelegateToName(getUserName(emp.getUserId()));
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setReason(request.getReason());
        entity.setStatus(1); // 生效中
        delegationMapper.insert(entity);

        log.info("委托创建成功: delegatorId={}, delegateToId={}, start={}, end={}",
                userId, request.getDelegateeId(), request.getStartTime(), request.getEndTime());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelDelegation(Long id, Long userId) {
        ApprovalDelegationEntity entity = delegationMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "委托记录不存在");
        }
        if (!entity.getDelegatorId().equals(userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "只能取消自己的委托");
        }
        if (entity.getStatus() != 1) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "该委托已失效，无法取消");
        }

        entity.setStatus(0); // 已取消
        delegationMapper.updateById(entity);

        log.info("委托已取消: id={}, delegatorId={}", id, userId);
    }

    /**
     * 查询当前用户的委托记录
     * <p>
     * 返回所有委托列表，并标记其中状态为"active"的作为当前生效委托。
     * 状态计算规则：status=0→cancelled（已取消）、status=1且未到期→active（生效中）、
     * status=1且已到期→expired（已过期）。
     * </p>
     *
     * @param userId 用户 ID
     * @return 委托列表 VO（含 activeDelegation 当前生效委托）
     */
    @Override
    public DelegationListVO findMyDelegations(Long userId) {
        List<ApprovalDelegationEntity> all = delegationMapper.selectList(
                Wrappers.lambdaQuery(ApprovalDelegationEntity.class)
                        .eq(ApprovalDelegationEntity::getDelegatorId, userId)
                        .orderByDesc(ApprovalDelegationEntity::getCreateTime)
        );

        LocalDateTime now = LocalDateTime.now();
        DelegationVO active = null;
        List<DelegationVO> records = new ArrayList<>();

        for (ApprovalDelegationEntity entity : all) {
            DelegationVO vo = toVO(entity, now);
            if ("active".equals(vo.getStatus())) {
                active = vo;
            }
            records.add(vo);
        }

        DelegationListVO result = new DelegationListVO();
        result.setActiveDelegation(active);
        result.setRecords(records);
        return result;
    }

    // ========== 内部方法 ==========

    /**
     * 根据用户ID查询真实姓名
     */
    private String getUserName(Long userId) {
        if (userId == null) return "";
        UserEntity user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : String.valueOf(userId);
    }

    /**
     * 委托实体转 VO（含状态计算）
     *
     * @param entity 委托实体
     * @param now    当前时间（用于计算过期状态）
     * @return 委托 VO
     */
    private DelegationVO toVO(ApprovalDelegationEntity entity, LocalDateTime now) {
        DelegationVO vo = new DelegationVO();
        vo.setId(entity.getId());
        vo.setDelegateeName(entity.getDelegateToName());
        vo.setStartTime(entity.getStartDate().format(DTF));
        vo.setEndTime(entity.getEndDate().format(DTF));
        vo.setReason(entity.getReason());

        // 查询被委托人职位信息
        if (entity.getDelegateToId() != null) {
            EmployeeBriefDTO emp = approvalEmployeeMapper.findByUserId(entity.getDelegateToId());
            if (emp != null) {
                vo.setPosition(emp.getPostName());
            }
        }

        // 计算状态
        if (entity.getStatus() == 0) {
            vo.setStatus("cancelled");
        } else if (entity.getStatus() == 1) {
            if (now.isBefore(entity.getEndDate())) {
                vo.setStatus("active");
            } else {
                vo.setStatus("expired");
            }
        } else {
            vo.setStatus("expired");
        }
        return vo;
    }
}
