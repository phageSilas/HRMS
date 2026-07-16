package com.hrms.business.personnel.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
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
import com.hrms.business.personnel.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.mapper.EntryApplicationMapper;
import com.hrms.business.personnel.service.EntryApplicationService;
import com.hrms.business.personnel.vo.EntryApplicationConfirmVO;
import com.hrms.business.personnel.vo.EntryApplicationPageVO;
import com.hrms.business.personnel.vo.EntryApplicationSubmitVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 入职申请服务实现
 */
@Service
@RequiredArgsConstructor
public class EntryApplicationServiceImpl implements EntryApplicationService {

    private static final ErrorCode ENTRY_APPLICATION_PHONE_DUPLICATE = new ErrorCode(40045, "手机号已存在入职申请");

    private static final ErrorCode ENTRY_APPLICATION_NOT_FOUND = new ErrorCode(40041, "入职申请不存在");

    private static final ErrorCode ENTRY_APPLICATION_NOT_DRAFT = new ErrorCode(40042, "非草稿状态无法修改");

    private static final ErrorCode ENTRY_APPLICATION_NOT_APPROVED = new ErrorCode(40044, "审批未通过，无法确认入职");

    private static final Map<Long, Object> ENTRY_CONFIRM_LOCKS = new ConcurrentHashMap<>();

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final int MAX_PAGE_SIZE = 200;

    private final EntryApplicationMapper entryApplicationMapper;

    // 注入
    private final ApprovalEngine approvalEngine;

    private final EmployeeService employeeService;

    /**
     * 分页查询入职申请。
     * @param queryDTO 入职申请查询参数
     * @return 入职申请分页列表
     */
    @Override
    public PageResult<EntryApplicationPageVO> pageEntryApplications(EntryApplicationQueryDTO queryDTO) {
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        Page<EntryApplicationEntity> page = entryApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                buildPageQueryWrapper(queryDTO)
        );
        List<EntryApplicationPageVO> records = page.getRecords().stream()
                .map(EntryApplicationConvert::toPageVO)
                .toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    /**
     * 创建入职申请。
     * @param requestDTO 入职申请创建参数
     * @return 入职申请详情
     */
    @Override
    public EntryApplicationPageVO createEntryApplication(EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        checkPhoneAvailable(requestDTO.getPhone(), null);
        EntryApplicationEntity entity = EntryApplicationConvert.toEntity(requestDTO);
        entity.setApprovalStatus(ApplicationStatusEnum.DRAFT.getCode());
        entryApplicationMapper.insert(entity);
        return EntryApplicationConvert.toPageVO(entity);
    }

    /**
     * 更新入职申请。
     * @param id 入职申请ID
     * @param requestDTO 入职申请更新参数
     */
    @Override
    public void updateEntryApplication(Long id, EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        EntryApplicationEntity entity = getRequiredEntryApplication(id);
        assertDraft(entity);
        checkPhoneAvailable(requestDTO.getPhone(), id);
        EntryApplicationConvert.fillEntity(entity, requestDTO);
        entryApplicationMapper.updateById(entity);
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

        // TODO 跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起入职审批。
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
        EntryApplicationEntity entity = getRequiredEntryApplication(id);
        if (entity.getApprovalStatus() != null && entity.getApprovalStatus() == ApplicationStatusEnum.ENTERED.getCode()) {
            return tempBuildConfirmedEmployee(entity);
        }
        Object confirmLock = getEntryConfirmLock(id);
        synchronized (confirmLock) {
            EntryApplicationEntity lockedEntity = getRequiredEntryApplication(id);
            if (lockedEntity.getApprovalStatus() != null
                    && lockedEntity.getApprovalStatus() == ApplicationStatusEnum.ENTERED.getCode()) {
                return tempBuildConfirmedEmployee(lockedEntity);
            }
            assertApproved(lockedEntity);
            // employeeService.generateEmployeeNo(lockedEntity); 本接口需要调用 hrms-business-employee 模块的生成员工工号接口
            lockedEntity.setActualHireDate(requestDTO.getActualHireDate());

            // TODO 跨模块调用已完成：当前调用 EmployeeService#createEmployee(createDTO) 创建员工档案。
            EmployeeEntity createdEmployee = createEmployeeFromEntryApplication(lockedEntity);

            Long employeeId = createdEmployee.getId();
            String employeeNo = createdEmployee.getEmployeeNo();

            // TODO 跨模块调用已完成：账号创建已由 EmployeeService#createEmployee(createDTO) 内部完成，personnel 不再重复调用 UserService#createUser(...)。

            lockedEntity.setEmployeeId(employeeId);
            lockedEntity.setEmployeeNo(employeeNo);
            lockedEntity.setApprovalStatus(ApplicationStatusEnum.ENTERED.getCode());
            entryApplicationMapper.updateById(lockedEntity);
            // entryConfirmedProducer.send(event); 本接口需要调用通知/MQ模块发送 personnel.entry.confirmed 事件和欢迎通知
            tempSendEntryConfirmedNotice(lockedEntity, employeeId, employeeNo);
            return buildConfirmVO(employeeId, employeeNo);
        }
    }

    /**
     * 获取入职确认本地锁。
     *
     * @param id 入职申请ID
     * @return 本地锁对象
     */
    private Object getEntryConfirmLock(Long id) {
        return ENTRY_CONFIRM_LOCKS.computeIfAbsent(id, key -> new Object());
    }

    /**
     * 临时构造已确认入职的员工信息。
     *
     * @param entity 入职申请实体
     * @return 入职确认结果
     */
    private EntryApplicationConfirmVO tempBuildConfirmedEmployee(EntryApplicationEntity entity) {
        return buildConfirmVO(entity.getId(), tempGenerateEmployeeNo(entity));
    }

    /**
     * 临时生成员工工号。
     *
     * @param entity 入职申请实体
     * @return 员工工号
     */
    private String tempGenerateEmployeeNo(EntryApplicationEntity entity) {
        return String.format("EMP%06d", entity.getId());
    }

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
     * 临时创建员工档案。
     *
     * @param entity 入职申请实体
     * @param employeeNo 员工工号
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
        // TODO 跨模块调用已完成：当前调用 EmployeeService#createEmployee(createDTO) 创建员工档案并由员工模块生成工号。
        return employeeService.createEmployee(createDTO);
    }

    /**
     * 临时发送入职确认通知。
     *
     * @param entity 入职申请实体
     * @param employeeId 员工ID
     * @param employeeNo 员工工号
     */
    private void tempSendEntryConfirmedNotice(EntryApplicationEntity entity, Long employeeId, String employeeNo) {
        // 临时空实现，等待通知模块或 RabbitMQ 事件生产者提供后替换。
    }

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
        LambdaQueryWrapper<EntryApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StrUtil.isNotBlank(queryDTO.getKeyword()), keywordWrapper -> keywordWrapper
                .like(EntryApplicationEntity::getCandidateName, queryDTO.getKeyword())
                .or()
                .like(EntryApplicationEntity::getPhone, queryDTO.getKeyword()));
        wrapper.eq(queryDTO.getApprovalStatus() != null, EntryApplicationEntity::getApprovalStatus, queryDTO.getApprovalStatus());
        wrapper.eq(queryDTO.getDepartmentId() != null, EntryApplicationEntity::getDeptId, queryDTO.getDepartmentId());
        wrapper.ge(queryDTO.getDateStart() != null, EntryApplicationEntity::getCreateTime,
                queryDTO.getDateStart() == null ? null : LocalDateTime.of(queryDTO.getDateStart(), LocalTime.MIN));
        wrapper.le(queryDTO.getDateEnd() != null, EntryApplicationEntity::getCreateTime,
                queryDTO.getDateEnd() == null ? null : LocalDateTime.of(queryDTO.getDateEnd(), LocalTime.MAX));
        wrapper.orderByDesc(EntryApplicationEntity::getCreateTime);
        return wrapper;
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

}
