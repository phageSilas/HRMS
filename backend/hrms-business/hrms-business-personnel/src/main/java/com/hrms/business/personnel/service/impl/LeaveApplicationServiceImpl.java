package com.hrms.business.personnel.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hrms.business.personnel.common.cache.PersonnelCacheKeys;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.approval.service.ApprovalTaskService;
import com.hrms.business.employee.dto.EmployeeQueryDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.employee.vo.EmployeeListVO;
import com.hrms.business.personnel.convert.LeaveApplicationConvert;
import com.hrms.business.personnel.dto.LeaveApplicationCreateRequestDTO;
import com.hrms.business.personnel.dto.LeaveApplicationQueryDTO;
import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.LeaveApplicationEntity;
import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.common.enums.LeaveTypeEnum;
import com.hrms.business.personnel.mapper.EmployeeSnapshotMapper;
import com.hrms.business.personnel.mapper.LeaveApplicationMapper;
import com.hrms.business.personnel.service.LeaveApplicationService;
import com.hrms.business.personnel.convert.PersonnelDisplayEnricher;
import com.hrms.business.personnel.vo.LeaveApplicationCreateVO;
import com.hrms.business.personnel.vo.LeaveApplicationPageVO;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hrms.business.personnel.common.constant.LeaveApplicationConstant.*;
import static com.hrms.business.personnel.common.enums.ServiceErrorCodeEnum.LEAVE_APPLICATION_DUPLICATE;
import static com.hrms.common.exception.ErrorCode.EMPLOYEE_NOT_FOUND;

/**
 * 离职申请服务实现
 */
@Service
@RequiredArgsConstructor
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    /**
     * personnel 分页缓存 TTL（分钟）
     */
    private static final long PAGE_CACHE_TTL_MINUTES = 2L;

    private final LeaveApplicationMapper leaveApplicationMapper;

    private final EmployeeService employeeService;

    private final ApprovalEngine approvalEngine;

    private final ApprovalTaskService approvalTaskService;

    private final DeptService deptService;

    private final PostService postService;

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    /**
     * 离职申请分页查询
     * @param queryDTO 离职申请查询参数
     * @return 离职申请分页列表
     */
    @Override
    public PageResult<LeaveApplicationPageVO> pageLeaveApplications(LeaveApplicationQueryDTO queryDTO) {
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        String cacheKey = PersonnelCacheKeys.leavePage(buildLeavePageCacheKey(queryDTO, pageNum, pageSize));
        PageResult<LeaveApplicationPageVO> cachedResult = getCachedLeavePage(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }
        // 构建人员显示 enricher
        PersonnelDisplayEnricher displayEnricher = new PersonnelDisplayEnricher(deptService, postService);
        Page<LeaveApplicationEntity> page = leaveApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                buildLeaveApplicationWrapper(queryDTO)
        );
        // 构建员工快照映射
        Map<Long, EmployeeSnapshotEntity> employeeSnapshotMap = listEmployeeSnapshotMap(collectEmployeeIds(page.getRecords()));
        // 构建离职申请分页列表
        List<LeaveApplicationPageVO> records = page.getRecords().stream()
                .map(entity -> displayEnricher.enrichLeaveApplication(
                        LeaveApplicationConvert.toPageVO(
                                entity,
                                employeeSnapshotMap.get(entity.getEmployeeId()),
                                employeeSnapshotMap.get(entity.getHandoverEmployeeId())),
                        employeeSnapshotMap.get(entity.getEmployeeId()) == null
                                ? null
                                : employeeSnapshotMap.get(entity.getEmployeeId()).getDeptId()))
                .toList();
        PageResult<LeaveApplicationPageVO> pageResult = PageResult.of(records, page.getTotal(), pageNum, pageSize);
        cacheLeavePage(cacheKey, pageResult);
        return pageResult;
    }

    /**
     * 创建离职申请
     * @param requestDTO 离职申请创建参数
     * @return 离职申请创建结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveApplicationCreateVO createLeaveApplication(LeaveApplicationCreateRequestDTO requestDTO) {
        // 获取员工快照
        EmployeeSnapshotEntity employeeSnapshot = getRequiredEmployeeSnapshot(requestDTO.getEmployeeId());
        // 提交防重
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        if (rt != null) {
            Boolean locked = rt.opsForValue()
                    .setIfAbsent(PersonnelCacheKeys.leaveSubmitToken(requestDTO.getEmployeeId()), "1", 30, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new GlobalException(LEAVE_APPLICATION_DUPLICATE, "该申请正在提交中，请勿重复操作");
            }
        }
        // 获取工作交接人快照
        getRequiredEmployeeSnapshot(requestDTO.getHandoverEmployeeId());
        // 确保员工没有进行中的离职申请
        assertNoProcessingLeaveApplication(requestDTO.getEmployeeId());

        LeaveApplicationEntity entity = new LeaveApplicationEntity();
        entity.setEmployeeId(employeeSnapshot.getId());
        entity.setLeaveType(LeaveTypeEnum.fromValue(requestDTO.getLeaveType()).getCode());
        entity.setLeaveReason(requestDTO.getLeaveReason());
        entity.setApplyDate(java.time.LocalDate.now());
        entity.setExpectedLastWorkDate(requestDTO.getLastWorkDate());
        entity.setHandoverEmployeeId(requestDTO.getHandoverEmployeeId());
        entity.setHandoverStatus(0);
        entity.setRemark(requestDTO.getRemark());
        entity.setApprovalStatus(ApplicationStatusEnum.APPROVING.getCode());
        leaveApplicationMapper.insert(entity);

        //  跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起离职审批。
        Long approvalInstanceId = approvalEngine.startApproval(
                ApprovalTypeEnum.LEAVE.getCode(),
                entity.getId(),
                JSONUtil.toJsonStr(entity),
                SecurityContextHolder.getUserId(),
                employeeSnapshot.getDeptId(),
                employeeSnapshot.getId()
        );
        entity.setApprovalInstanceId(approvalInstanceId);
        leaveApplicationMapper.updateById(entity);
        evictLeavePageCache();

        return LeaveApplicationCreateVO.builder()
                .id(entity.getId())
                .build();
    }

    /**
     * 快速审批通过离职申请。
     *
     * @param id 离职申请ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quickApproveLeaveApplication(Long id) {
        LeaveApplicationEntity entity = getRequiredLeaveApplication(id);
        assertApproving(entity.getApprovalStatus(), "当前离职申请不是审批中状态，无法快速审批");
        processQuickApprove(entity.getApprovalInstanceId(), "当前离职申请无有效审批实例，无法快速审批");
        evictLeavePageCache();
    }

    /**
     * 临时发起离职审批。
     *
     * @param employeeSnapshot 员工快照
     * @return 审批实例ID
     * 本方法使用的工具类: IdUtil(hutool)
     */
    //private Long tempStartLeaveApproval(EmployeeSnapshotEntity employeeSnapshot) {
    //    return IdUtil.getSnowflakeNextId();
    //}

    /**
     * 查询必须存在的员工快照。
     *
     * @param employeeId 员工ID
     * @return 员工快照
     * 本方法使用的工具类: 无
     */
    private EmployeeSnapshotEntity getRequiredEmployeeSnapshot(Long employeeId) {
        //  跨模块调用已完成：当前调用 EmployeeService#getEmployeeBrief(employeeId) 获取员工简要信息。
        EmployeeEntity employee = employeeService.getEmployeeBrief(employeeId);
        if (employee == null) {
            throw new GlobalException(EMPLOYEE_NOT_FOUND);
        }
        return toEmployeeSnapshot(employee);
    }

    /**
     * 将员工模块实体转换为本模块员工快照。
     *
     * @param employee 员工模块实体
     * @return 本模块员工快照
     * 本方法使用的工具类: 无
     */
    private EmployeeSnapshotEntity toEmployeeSnapshot(EmployeeEntity employee) {
        EmployeeSnapshotEntity snapshot = new EmployeeSnapshotEntity();
        snapshot.setId(employee.getId());
        snapshot.setEmployeeNo(employee.getEmployeeNo());
        snapshot.setEmployeeName(employee.getEmployeeName());
        snapshot.setDeptId(employee.getDeptId());
        snapshot.setPostId(employee.getPostId());
        snapshot.setLeaderId(employee.getLeaderId());
        snapshot.setJobLevel(employee.getJobLevel());
        snapshot.setEmploymentStatus(employee.getEmploymentStatus());
        snapshot.setHireDate(employee.getHireDate());
        snapshot.setProbationMonth(employee.getProbationMonth());
        snapshot.setBaseSalary(employee.getBaseSalary());
        return snapshot;
    }

    /**
     * 校验员工没有进行中的离职申请。
     *
     * @param employeeId 员工ID
     * 本方法使用的工具类: 无
     */
    private void assertNoProcessingLeaveApplication(Long employeeId) {
        Long count = leaveApplicationMapper.selectCount(new LambdaQueryWrapper<LeaveApplicationEntity>()
                .eq(LeaveApplicationEntity::getEmployeeId, employeeId)

                //AND approval_status IN (?, ?),ApprovalStatus会被自动转换为ApprovalStatusEnum
                .in(LeaveApplicationEntity::getApprovalStatus,
                        ApplicationStatusEnum.DRAFT.getCode(), ApplicationStatusEnum.APPROVING.getCode()));
        if (count != null && count > 0) {
            throw new GlobalException(LEAVE_APPLICATION_DUPLICATE);
        }
    }

    /**
     * 查询必定存在的离职申请。
     *
     * @param id 离职申请ID
     * @return 离职申请实体
     */
    private LeaveApplicationEntity getRequiredLeaveApplication(Long id) {
        LeaveApplicationEntity entity = leaveApplicationMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(EMPLOYEE_NOT_FOUND, "离职申请不存在");
        }
        return entity;
    }

    /**
     * 校验申请是否处于审批中。
     *
     * @param approvalStatus 审批状态
     * @param errorMessage 错误提示
     */
    private void assertApproving(Integer approvalStatus, String errorMessage) {
        if (approvalStatus == null || approvalStatus != ApplicationStatusEnum.APPROVING.getCode()) {
            throw new GlobalException(com.hrms.common.exception.ErrorCode.BUSINESS_ERROR, errorMessage);
        }
    }

    /**
     * 通过审批引擎执行快速审批通过。
     *
     * @param approvalInstanceId 审批实例ID
     * @param missingInstanceMessage 审批实例缺失提示
     */
    private void processQuickApprove(Long approvalInstanceId, String missingInstanceMessage) {
        if (approvalInstanceId == null) {
            throw new GlobalException(com.hrms.common.exception.ErrorCode.BUSINESS_ERROR, missingInstanceMessage);
        }
        Long pendingTaskId = approvalTaskService.getCurrentPendingTaskIdByInstanceId(approvalInstanceId);
        if (pendingTaskId == null) {
            throw new GlobalException(com.hrms.common.exception.ErrorCode.BUSINESS_ERROR, "当前审批实例不存在待办审批任务，无法快速审批");
        }
        approvalEngine.processAction(pendingTaskId, "approve", "快速审批通过", null);
    }

    /**
     * 构建离职申请分页查询条件。
     *
     * @param queryDTO 离职申请查询参数
     * @return 查询条件
     * 本方法使用的工具类: StrUtil(hutool),CollUtil(hutool)
     */
    private LambdaQueryWrapper<LeaveApplicationEntity> buildLeaveApplicationWrapper(LeaveApplicationQueryDTO queryDTO) {
        LambdaQueryWrapper<LeaveApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        // 如果离职类型不为空白，则添加查询条件,StrUtil.isNotBlank() - 检查字符串是否非空且非空白
        if (StrUtil.isNotBlank(queryDTO.getLeaveType())) {
            wrapper.eq(LeaveApplicationEntity::getLeaveType, LeaveTypeEnum.fromValue(queryDTO.getLeaveType()).getCode());
        }

        wrapper.eq(queryDTO.getApprovalStatus() != null, LeaveApplicationEntity::getApprovalStatus, queryDTO.getApprovalStatus());
        if (queryDTO.getDepartmentId() != null || StrUtil.isNotBlank(queryDTO.getKeyword())) {
            List<Long> employeeIds = listEmployeeIdsByQuery(queryDTO);
            // 如果部门ID不为空或关键词不为空，则添加查询条件,CollUtil.isNotEmpty() - 检查集合是否非空且有元素
            wrapper.in(CollUtil.isNotEmpty(employeeIds), LeaveApplicationEntity::getEmployeeId, employeeIds);
            wrapper.eq(CollUtil.isEmpty(employeeIds), LeaveApplicationEntity::getEmployeeId, IMPOSSIBLE_EMPLOYEE_ID);
        }
        wrapper.orderByDesc(LeaveApplicationEntity::getCreateTime);
        return wrapper;
    }

    /**
     * 根据查询条件获取员工ID列表。
     *
     * @param queryDTO 离职申请查询参数
     * @return 员工ID列表
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private List<Long> listEmployeeIdsByQuery(LeaveApplicationQueryDTO queryDTO) {
        // 跨模块调用已完成：当前调用 EmployeeService#listEmployees(employeeQueryDTO) 按部门和关键词查询员工列表。
        EmployeeQueryDTO employeeQueryDTO = new EmployeeQueryDTO();
        employeeQueryDTO.setKeyword(queryDTO.getKeyword());
        employeeQueryDTO.setDeptIds(resolveTargetDeptIds(queryDTO.getDepartmentId()));
        employeeQueryDTO.setPageNum(1);
        employeeQueryDTO.setPageSize(MAX_PAGE_SIZE);
        return employeeService.listEmployees(employeeQueryDTO).getRecords().stream()
                .map(EmployeeListVO::getId)
                .filter(Objects::nonNull) // 过滤掉空值
                .toList();
    }

    /**
     * 收集离职申请涉及的员工ID。
     *
     * @param records 离职申请列表
     * @return 员工ID列表
     * 本方法使用的工具类: Stream(JDK)
     */
    private List<Long> collectEmployeeIds(List<LeaveApplicationEntity> records) {
        return records.stream()
                .flatMap(entity -> Stream.of(entity.getEmployeeId(), entity.getHandoverEmployeeId())) // 获取员工ID和交接人ID
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 批量查询员工快照映射。
     *
     * @return 员工快照映射
     * 本方法使用的工具类: CollUtil(hutool)
     */
    private List<Long> resolveTargetDeptIds(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        List<Long> deptIds = deptService.getSubDeptIds(departmentId);
        if (CollUtil.isEmpty(deptIds)) {
            return List.of(departmentId);
        }
        return deptIds;
    }

    private Map<Long, EmployeeSnapshotEntity> listEmployeeSnapshotMap(List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        //  跨模块调用已完成：调用当前员工模块批量快照接口，用 EmployeeService#getEmployeeBrief(employeeId) 补全。
        return employeeIds.stream()
                .map(employeeService::getEmployeeBrief)
                .filter(Objects::nonNull)
                .map(this::toEmployeeSnapshot)
                .collect(Collectors.toMap(EmployeeSnapshotEntity::getId, Function.identity(), (left, right) -> left));
    }

    /**
     * 规范化页码。
     *
     * @param pageNum 页码
     * @return 有效页码
     * 本方法使用的工具类: 无
     */
    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    /**
     * 规范化每页条数。
     *
     * @param pageSize 每页条数
     * @return 有效每页条数
     * 本方法使用的工具类: Math(JDK)
     */
    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /**
     * 读取离职分页缓存。
     *
     * @param cacheKey 缓存 Key
     * @return 分页结果
     */
    @SuppressWarnings("unchecked")
    private PageResult<LeaveApplicationPageVO> getCachedLeavePage(String cacheKey) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return null;
        }
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isBlank(cached)) {
            return null;
        }
        return JSONUtil.toBean(cached, PageResult.class);
    }

    /**
     * 写入离职分页缓存。
     *
     * @param cacheKey 缓存 Key
     * @param pageResult 分页结果
     */
    private void cacheLeavePage(String cacheKey, PageResult<LeaveApplicationPageVO> pageResult) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(pageResult), PAGE_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 清理离职分页缓存。
     */
    private void evictLeavePageCache() {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        Set<String> keys = redisTemplate.keys(PersonnelCacheKeys.leavePagePattern());
        if (keys == null || keys.isEmpty()) {
            return;
        }
        redisTemplate.delete(keys);
    }

    /**
     * 构建稳定的离职分页缓存 Key。
     *
     * @param queryDTO 查询参数
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 规范化缓存 Key
     */
    private String buildLeavePageCacheKey(LeaveApplicationQueryDTO queryDTO, int pageNum, int pageSize) {
        return StrUtil.join("|",
                normalizeCacheValue(queryDTO.getKeyword()),
                normalizeCacheValue(queryDTO.getDepartmentId()),
                normalizeCacheValue(queryDTO.getLeaveType()),
                normalizeCacheValue(queryDTO.getApprovalStatus()),
                pageNum,
                pageSize);
    }

    /**
     * 规范化缓存维度，避免等价查询生成不同 Key。
     *
     * @param value 原始值
     * @return 规范化后的值
     */
    private String normalizeCacheValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String str) {
            return StrUtil.isBlank(str) ? "blank" : str.trim();
        }
        return String.valueOf(value);
    }

}
