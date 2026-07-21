package com.hrms.business.personnel.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hrms.business.personnel.common.cache.PersonnelCacheKeys;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.employee.dto.EmployeeCreateDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.personnel.convert.EntryApplicationConvert;
import com.hrms.business.personnel.dto.EntryApplicationConfirmRequestDTO;
import com.hrms.business.personnel.dto.EntryApplicationCreateOrUpdateRequestDTO;
import com.hrms.business.personnel.dto.EntryApplicationQueryDTO;
import com.hrms.business.personnel.entity.EntryApplicationEntity;
import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.mapper.EntryApplicationMapper;
import com.hrms.business.personnel.convert.PersonnelDisplayEnricher;
import com.hrms.business.personnel.service.EntryApplicationService;
import com.hrms.business.personnel.vo.EntryApplicationConfirmVO;
import com.hrms.business.personnel.vo.EntryApplicationPageVO;
import com.hrms.business.personnel.vo.EntryApplicationStatsVO;
import com.hrms.business.personnel.vo.EntryApplicationSubmitVO;
import com.hrms.common.exception.ErrorCode;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.hrms.business.personnel.common.constant.EntryApplicationConstant.*;
import static com.hrms.business.personnel.common.enums.ServiceErrorCodeEnum.*;

/**
 * 入职申请服务实现
 */
@Service
@RequiredArgsConstructor
public class EntryApplicationServiceImpl implements EntryApplicationService {

    private final EntryApplicationMapper entryApplicationMapper;

    private final ApprovalEngine approvalEngine;

    private final EmployeeService employeeService;

    private final DeptService deptService;

    private final PostService postService;

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    /**
     * 分页查询入职申请。
     * @param queryDTO 入职申请查询参数
     * @return 入职申请分页列表
     */
    @Override
    public PageResult<EntryApplicationPageVO> pageEntryApplications(EntryApplicationQueryDTO queryDTO) {
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        PersonnelDisplayEnricher displayEnricher = new PersonnelDisplayEnricher(deptService, postService);
        Page<EntryApplicationEntity> page = entryApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                buildPageQueryWrapper(queryDTO)
        );
        List<EntryApplicationPageVO> records = page.getRecords().stream()
                .map(EntryApplicationConvert::toPageVO)// 转换为页面VO
                .map(displayEnricher::enrichEntryApplication)// 填充关联信息
                .toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public EntryApplicationStatsVO statsEntryApplications(EntryApplicationQueryDTO queryDTO) {
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        int queryHash = queryDTO.hashCode();
        if (rt != null) {
            String cached = rt.opsForValue().get(PersonnelCacheKeys.entryStats(queryHash));
            if (StrUtil.isNotBlank(cached)) {
                return JSONUtil.toBean(cached, EntryApplicationStatsVO.class);
            }
        }
        EntryApplicationStatsVO stats = EntryApplicationStatsVO.builder()
                .all(countByStatus(queryDTO, null))
                .draft(countByStatus(queryDTO, ApplicationStatusEnum.DRAFT.getCode()))
                .approving(countByStatus(queryDTO, ApplicationStatusEnum.APPROVING.getCode()))
                .approved(countByStatus(queryDTO, ApplicationStatusEnum.APPROVED.getCode()))
                .rejected(countByStatus(queryDTO, ApplicationStatusEnum.REJECTED.getCode()))
                .entered(countByStatus(queryDTO, ApplicationStatusEnum.ENTERED.getCode()))
                .build();
        if (rt != null) {
            rt.opsForValue().set(PersonnelCacheKeys.entryStats(queryHash),
                    JSONUtil.toJsonStr(stats), 1, TimeUnit.MINUTES);
        }
        return stats;
    }

    /**
     * 查询单个入职申请详情。
     *
     * @param id 入职申请ID
     * @return 入职申请详情
     */
    @Override
    public EntryApplicationPageVO getEntryApplication(Long id) {
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        if (rt != null) {
            String cached = rt.opsForValue().get(PersonnelCacheKeys.entryDetail(id));
            if (StrUtil.isNotBlank(cached)) {
                return JSONUtil.toBean(cached, EntryApplicationPageVO.class);
            }
        }
        EntryApplicationPageVO vo = enrichEntryApplication(getRequiredEntryApplication(id));
        if (rt != null) {
            rt.opsForValue().set(PersonnelCacheKeys.entryDetail(id),
                    JSONUtil.toJsonStr(vo), 5, TimeUnit.MINUTES);
        }
        return vo;
    }

    /**
     * 创建入职申请。
     * @param requestDTO 入职申请创建参数
     * @return 入职申请详情
     */
    @Override
    public EntryApplicationPageVO createEntryApplication(EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        // 确保手机号唯一
        checkPhoneAvailable(requestDTO.getPhone(), null);
        EntryApplicationEntity entity = EntryApplicationConvert.toEntity(requestDTO);
        entity.setApprovalStatus(ApplicationStatusEnum.DRAFT.getCode());
        entryApplicationMapper.insert(entity);
        // 转换为页面VO并填充关联信息
        return new PersonnelDisplayEnricher(deptService, postService)
                .enrichEntryApplication(EntryApplicationConvert.toPageVO(entity));
    }

    /**
     * 更新入职申请。
     * @param id 入职申请ID
     * @param requestDTO 入职申请更新参数
     */
    @Override
    public EntryApplicationPageVO updateEntryApplication(Long id, EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        EntryApplicationEntity entity = getRequiredEntryApplication(id);
        // 确保状态为草稿
        assertDraft(entity);
        // 提交防重
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        if (rt != null) {
            Boolean locked = rt.opsForValue()
                    .setIfAbsent(PersonnelCacheKeys.entrySubmitToken(id), "1", 30, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new GlobalException(ErrorCode.CONFLICT, "入职申请正在提交中，请勿重复操作");
            }
        }

        // 确保手机号唯一
        checkPhoneAvailable(requestDTO.getPhone(), id);
        // 填充更新内容
        EntryApplicationConvert.fillEntity(entity, requestDTO);
        entryApplicationMapper.updateById(entity);
        return enrichEntryApplication(entity);
    }

    /**
     * 提交入职申请。
     * @param id 入职申请ID
     * @return 入职申请提交结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntryApplicationSubmitVO submitEntryApplication(Long id) {
        EntryApplicationEntity entity = getRequiredEntryApplication(id);
        assertDraft(entity);
        // 提交防重
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        if (rt != null) {
            Boolean locked = rt.opsForValue()
                    .setIfAbsent(PersonnelCacheKeys.entrySubmitToken(id), "1", 30, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new GlobalException(ErrorCode.CONFLICT, "入职申请正在提交中，请勿重复操作");
            }
        }


        // 跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起入职审批。
        Long approvalInstanceId = approvalEngine.startApproval(
                ApprovalTypeEnum.ENTRY.getCode(),       // approvalType = "ENTRY"
                entity.getId(),                          // bizId
                JSONUtil.toJsonStr(entity),              // formData（JSON 快照）
                SecurityContextHolder.getUserId(), // applicantUserId
                entity.getDeptId(),                      // applicantDeptId
                null                                     // applicantEmployeeId（入职前尚无员工ID）
        );
        entity.setApprovalInstanceId(approvalInstanceId);
        entity.setApprovalStatus(ApplicationStatusEnum.APPROVING.getCode());
        entryApplicationMapper.updateById(entity);

        return EntryApplicationSubmitVO.builder()
                .approvalInstanceId(approvalInstanceId)
                .approvalStatus(entity.getApprovalStatus())
                .build();
    }

    /**
     * 临时发起入职审批。
     *
     * @param entity 入职申请实体
     * @return 审批实例ID
     */
    // private Long tempStartEntryApproval(EntryApplicationEntity entity) {
    //     return IdUtil.getSnowflakeNextId();
    // }

    /**
     * 确认入职申请。
     * @param id 入职申请ID
     * @param requestDTO 入职申请确认参数
     * @return 入职申请确认结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntryApplicationConfirmVO confirmEntryApplication(Long id, EntryApplicationConfirmRequestDTO requestDTO) {
        StringRedisTemplate rt = redisTemplateProvider.getIfAvailable();
        Boolean lockOk = rt == null ? Boolean.TRUE :
                rt.opsForValue().setIfAbsent(PersonnelCacheKeys.entryConfirmLock(id), "1", 10, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(lockOk)) {
            throw new GlobalException(ErrorCode.CONFLICT, "入职申请正在确认中，请稍后重试");
        }
        try {
            EntryApplicationEntity entity = getRequiredEntryApplication(id);
            if (entity.getApprovalStatus() != null && entity.getApprovalStatus() == ApplicationStatusEnum.ENTERED.getCode()) {
                return buildConfirmedEmployee(entity);
            }
            assertApproved(entity);
            // 设置实际入职日期
            entity.setActualHireDate(requestDTO.getActualHireDate());

            //  跨模块调用已完成：当前调用 EmployeeService#createEmployee(createDTO) 创建员工档案。
            EmployeeEntity createdEmployee = createEmployeeFromEntryApplication(entity);

            Long employeeId = createdEmployee.getId();
            String employeeNo = createdEmployee.getEmployeeNo();

            //  跨模块调用已完成：账号创建已由 EmployeeService#createEmployee(createDTO) 内部完成。
            entity.setEmployeeId(employeeId);
            entity.setEmployeeNo(employeeNo);
            entity.setApprovalStatus(ApplicationStatusEnum.ENTERED.getCode());
            entryApplicationMapper.updateById(entity);
            evictEntryDetailCache(id);

            return buildConfirmVO(employeeId, employeeNo);
        } finally {
            if (rt != null) {
                rt.delete(PersonnelCacheKeys.entryConfirmLock(id));
            }
        }
    }

    /**
     * 临时构造已确认入职的员工信息。
     *
     * @param entity 入职申请实体
     * @return 入职确认结果
     */
    private EntryApplicationConfirmVO buildConfirmedEmployee(EntryApplicationEntity entity) {
        if (entity.getEmployeeId() == null || StrUtil.isBlank(entity.getEmployeeNo())) {
            throw new GlobalException(ENTRY_APPLICATION_EMPLOYEE_MISSING);
        }
        return buildConfirmVO(entity.getEmployeeId(), entity.getEmployeeNo());
    }

    // 已停用：员工工号由 EmployeeService#createEmployee(createDTO) 统一生成。
    //     /**
    //      * 临时生成员工工号。
    //      *
    //      * @param entity 入职申请实体
    //      * @return 员工工号
    //      */
    //     private String tempGenerateEmployeeNo(EntryApplicationEntity entity) {
    //         return String.format("EMP%06d", entity.getId());
    //     }

    // 已停用：账号创建已由 EmployeeService#createEmployee(createDTO) 内部完成，避免重复创建手机号账号。
    //     /**
    //      * 临时创建入职账号。
    //      *
    //      * @param entity 入职申请实体
    //      * @param employeeNo 员工工号
    //      * @param employeeId 员工ID
    //      * 本方法使用的工具类: UserService(hrms-system-auth)
    //      */
    //     private void tempCreateEntryAccount(EntryApplicationEntity entity, String employeeNo, Long employeeId) {
    //         UserCreateDTO createDTO = new UserCreateDTO();
    //         createDTO.setUsername(employeeNo);
    //         createDTO.setPassword(entity.getPhone().substring(entity.getPhone().length() - 6));
    //         createDTO.setRealName(entity.getCandidateName());
    //         createDTO.setPhone(entity.getPhone());
    //         createDTO.setEmail(entity.getEmail());
    //         createDTO.setEmployeeId(employeeId);
        // 当前 auth 模块尚无“入职默认角色/按岗位分配角色”公开接口，roleIds 暂不传，由账号或权限模块后续补齐。
    //         userService.createUser(createDTO);
    //     }

    /**
     * 创建员工档案。
     *
     * @param entity 入职申请实体
     * @return 员工模块创建后的员工实体
     * 本方法使用的工具类: EmployeeService(hrms-business-employee)
     */
    private EmployeeEntity createEmployeeFromEntryApplication(EntryApplicationEntity entity) {
        EmployeeCreateDTO createDTO = new EmployeeCreateDTO();
        createDTO.setEmployeeName(entity.getCandidateName());
        createDTO.setGender(entity.getGender());
        createDTO.setPhone(entity.getPhone());
        createDTO.setEmail(entity.getEmail());
        createDTO.setDeptId(entity.getDeptId());
        createDTO.setPostId(entity.getPostId());
        createDTO.setLeaderId(entity.getLeaderId());
        createDTO.setHireType(entity.getHireType());
        createDTO.setHireDate(entity.getActualHireDate() == null ? entity.getExpectedHireDate() : entity.getActualHireDate());
        createDTO.setProbationMonth(entity.getProbationMonth());
        createDTO.setProbationSalaryRatio(entity.getProbationSalaryRatio());
        createDTO.setIdCardNo(entity.getIdCardNo());
        createDTO.setRemark("由入职申请确认创建，申请ID：" + entity.getId());
        // 跨模块调用已完成：当前调用 EmployeeService#createEmployee(createDTO) 创建员工档案并由员工模块生成工号。
        return employeeService.createEmployee(createDTO);
    }

    /**
     * 临时发送入职确认通知。
     *
     * @param entity 入职申请实体
     * @param employeeId 员工ID
     * @param employeeNo 员工工号
     */
    //private void tempSendEntryConfirmedNotice(EntryApplicationEntity entity, Long employeeId, String employeeNo) {
    //    // 临时空实现，等待通知模块或 RabbitMQ 事件生产者提供后替换。
    //}

    /**
     * 构造入职确认结果。
     *
     * @param employeeId 员工ID
     * @param employeeNo 员工工号
     * @return 入职确认结果
     */
    private EntryApplicationConfirmVO buildConfirmVO(Long employeeId, String employeeNo) {
        return EntryApplicationConfirmVO.builder()
                .employeeId(employeeId)
                .employeeNo(employeeNo)
                .build();
    }

    /**
     * 查询必定存在的入职申请。
     *
     * @param id 入职申请ID
     * @return 入职申请实体
     */
    private EntryApplicationEntity getRequiredEntryApplication(Long id) {
        EntryApplicationEntity entity = entryApplicationMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ENTRY_APPLICATION_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 转换并补齐入职申请展示字段。
     *
     * @param entity 入职申请实体
     * @return 页面展示VO
     */
    private EntryApplicationPageVO enrichEntryApplication(EntryApplicationEntity entity) {
        return new PersonnelDisplayEnricher(deptService, postService)
                .enrichEntryApplication(EntryApplicationConvert.toPageVO(entity));
    }

    /**
     * 校验入职申请是否为草稿。
     *
     * @param entity 入职申请实体
     */
    private void assertDraft(EntryApplicationEntity entity) {
        if (entity.getApprovalStatus() == null || entity.getApprovalStatus() != ApplicationStatusEnum.DRAFT.getCode()) {
            throw new GlobalException(ENTRY_APPLICATION_NOT_DRAFT);
        }
    }

    /**
     * 校验入职申请审批是否已通过。
     *
     * @param entity 入职申请实体
     */
    private void assertApproved(EntryApplicationEntity entity) {
        if (entity.getApprovalStatus() == null || entity.getApprovalStatus() != ApplicationStatusEnum.APPROVED.getCode()) {
            throw new GlobalException(ENTRY_APPLICATION_NOT_APPROVED);
        }
    }

    /**
     * 校验入职申请手机号是否可用。
     *
     * @param phone 手机号
     * @param excludeId 排除的入职申请ID
     */
    private void checkPhoneAvailable(String phone, Long excludeId) {
        Long count = entryApplicationMapper.selectCount(new LambdaQueryWrapper<EntryApplicationEntity>()
                .eq(EntryApplicationEntity::getPhone, phone)
                .ne(excludeId != null, EntryApplicationEntity::getId, excludeId));
        if (count != null && count > 0) {
            throw new GlobalException(ENTRY_APPLICATION_PHONE_DUPLICATE);
        }
    }

    /**
     * 构建入职申请分页查询条件。
     *
     * @param queryDTO 入职申请查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<EntryApplicationEntity> buildPageQueryWrapper(EntryApplicationQueryDTO queryDTO) {
        return buildPageQueryWrapper(queryDTO, queryDTO.getApprovalStatus());
    }

    private LambdaQueryWrapper<EntryApplicationEntity> buildPageQueryWrapper(EntryApplicationQueryDTO queryDTO,
                                                                             Integer approvalStatus) {
        List<Long> targetDeptIds = resolveTargetDeptIds(queryDTO.getDepartmentId());
        LambdaQueryWrapper<EntryApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StrUtil.isNotBlank(queryDTO.getKeyword()), keywordWrapper -> keywordWrapper
                .like(EntryApplicationEntity::getCandidateName, queryDTO.getKeyword())
                .or()
                .like(EntryApplicationEntity::getPhone, queryDTO.getKeyword()));
        // 根据审批状态进行过滤
        wrapper.eq(approvalStatus != null, EntryApplicationEntity::getApprovalStatus, approvalStatus);
        // 根据部门ID进行过滤
        wrapper.in(!targetDeptIds.isEmpty(), EntryApplicationEntity::getDeptId, targetDeptIds);
        // 根据申请日期起始进行过滤
        wrapper.ge(queryDTO.getDateStart() != null, EntryApplicationEntity::getCreateTime,
                queryDTO.getDateStart() == null ? null : LocalDateTime.of(queryDTO.getDateStart(), LocalTime.MIN));
        // 根据申请日期结束进行过滤
        wrapper.le(queryDTO.getDateEnd() != null, EntryApplicationEntity::getCreateTime,
                queryDTO.getDateEnd() == null ? null : LocalDateTime.of(queryDTO.getDateEnd(), LocalTime.MAX));
        // 按创建时间降序排列
        wrapper.orderByDesc(EntryApplicationEntity::getCreateTime);
        return wrapper;
    }

    private Long countByStatus(EntryApplicationQueryDTO queryDTO, Integer approvalStatus) {
        Long count = entryApplicationMapper.selectCount(buildPageQueryWrapper(queryDTO, approvalStatus));
        return count == null ? 0L : count;
    }

    /**
     * 解析部门树范围ID，包含所选部门自身及全部子孙部门。
     *
     * @param departmentId 所选部门ID
     * @return 部门树范围ID列表
     */
    private List<Long> resolveTargetDeptIds(Long departmentId) {
        if (departmentId == null) {
            return List.of();
        }
        List<Long> deptIds = deptService.getSubDeptIds(departmentId);
        if (deptIds == null || deptIds.isEmpty()) {
            return List.of(departmentId);
        }
        return new ArrayList<>(Set.copyOf(deptIds));
    }

    /**
     * 规范化页码。
     *
     * @param pageNum 页码
     * @return 有效页码
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
     */
    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /**
     * 清除入职申请详情缓存
     *
     * @param id 入职申请ID
     */
    private void evictEntryDetailCache(Long id) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.delete(PersonnelCacheKeys.entryDetail(id));
        }
    }
}
