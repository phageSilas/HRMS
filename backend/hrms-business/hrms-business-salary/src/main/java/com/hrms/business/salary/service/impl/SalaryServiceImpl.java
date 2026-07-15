package com.hrms.business.salary.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendancePayrollSourceVO;
import com.hrms.business.salary.cache.SalaryCacheKeys;
import com.hrms.business.salary.dto.EmployeeSalaryProfileRequestDTO;
import com.hrms.business.salary.dto.SalaryBatchCreateRequestDTO;
import com.hrms.business.salary.dto.SalaryPayslipVerifyRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateCreateOrUpdateRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateItemRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateQueryDTO;
import com.hrms.business.salary.entity.EmployeeSalaryProfileEntity;
import com.hrms.business.salary.entity.SalaryBatchEntity;
import com.hrms.business.salary.entity.SalaryBatchItemEntity;
import com.hrms.business.salary.entity.SalaryEmployeeSnapshotEntity;
import com.hrms.business.salary.entity.SalarySysUserEntity;
import com.hrms.business.salary.entity.SalaryTemplateEntity;
import com.hrms.business.salary.entity.SalaryTemplateItemEntity;
import com.hrms.business.salary.enums.SalaryBatchStatusEnum;
import com.hrms.business.salary.enums.SalaryWarningLevelEnum;
import com.hrms.business.salary.mapper.EmployeeSalaryProfileMapper;
import com.hrms.business.salary.mapper.SalaryBatchItemMapper;
import com.hrms.business.salary.mapper.SalaryBatchMapper;
import com.hrms.business.salary.mapper.SalaryEmployeeSnapshotMapper;
import com.hrms.business.salary.mapper.SalarySysUserMapper;
import com.hrms.business.salary.mapper.SalaryTemplateItemMapper;
import com.hrms.business.salary.mapper.SalaryTemplateMapper;
import com.hrms.business.salary.mq.SalaryBatchCalculateMessage;
import com.hrms.business.salary.mq.SalaryMqConstants;
import com.hrms.business.salary.service.SalaryService;
import com.hrms.business.salary.vo.EmployeeSalaryProfileVO;
import com.hrms.business.salary.vo.SalaryBatchItemVO;
import com.hrms.business.salary.vo.SalaryBatchPreviewVO;
import com.hrms.business.salary.vo.SalaryBatchVO;
import com.hrms.business.salary.vo.SalaryPayslipDetailVO;
import com.hrms.business.salary.vo.SalaryPayslipListVO;
import com.hrms.business.salary.vo.SalaryPayslipVerifyVO;
import com.hrms.business.salary.vo.SalaryTemplateItemVO;
import com.hrms.business.salary.vo.SalaryTemplatePageVO;
import com.hrms.business.salary.vo.SalaryTrendVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 薪资管理服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final Set<String> PAYSLIP_VISIBLE_STATUS = Set.of(
            SalaryBatchStatusEnum.APPROVING.name(),
            SalaryBatchStatusEnum.APPROVED.name(),
            SalaryBatchStatusEnum.RELEASED.name(),
            SalaryBatchStatusEnum.ARCHIVED.name()
    );

    private final SalaryTemplateMapper salaryTemplateMapper;
    private final SalaryTemplateItemMapper salaryTemplateItemMapper;
    private final EmployeeSalaryProfileMapper employeeSalaryProfileMapper;
    private final SalaryBatchMapper salaryBatchMapper;
    private final SalaryBatchItemMapper salaryBatchItemMapper;
    private final SalaryEmployeeSnapshotMapper employeeSnapshotMapper;
    private final SalarySysUserMapper salarySysUserMapper;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final ObjectProvider<AttendanceService> attendanceServiceProvider;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    /**
     * 分页查询薪资账套。
     *
     * @param queryDTO 查询参数
     * @return 薪资账套分页结果
     * 本方法使用的工具类: Page(MyBatis-Plus),Wrappers(MyBatis-Plus),StrUtil(hutool),PageResult(hrms-common)
     */
    @Override
    public PageResult<SalaryTemplatePageVO> pageTemplates(SalaryTemplateQueryDTO queryDTO) {
        LambdaQueryWrapper<SalaryTemplateEntity> wrapper = Wrappers.lambdaQuery(SalaryTemplateEntity.class)
                .like(StrUtil.isNotBlank(queryDTO.getTemplateName()), SalaryTemplateEntity::getTemplateName,
                        queryDTO.getTemplateName())
                .eq(queryDTO.getStatus() != null, SalaryTemplateEntity::getStatus, queryDTO.getStatus())
                .orderByDesc(SalaryTemplateEntity::getCreateTime);
        Page<SalaryTemplateEntity> page = salaryTemplateMapper.selectPage(
                Page.of(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<Long> templateIds = page.getRecords().stream().map(SalaryTemplateEntity::getId).toList();
        Map<Long, List<SalaryTemplateItemEntity>> itemMap = listTemplateItems(templateIds).stream()
                .collect(Collectors.groupingBy(SalaryTemplateItemEntity::getTemplateId));
        List<SalaryTemplatePageVO> records = page.getRecords().stream()
                .map(entity -> toTemplateVO(entity, itemMap.getOrDefault(entity.getId(), List.of())))
                .toList();
        return PageResult.of(records, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 创建薪资账套。
     *
     * @param requestDTO 创建请求
     * @return 创建后的薪资账套
     * 本方法使用的工具类: IdUtil(hutool),StrUtil(hutool),CollUtil(hutool),Transactional(Spring)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryTemplatePageVO createTemplate(SalaryTemplateCreateOrUpdateRequestDTO requestDTO) {
        SalaryTemplateEntity entity = new SalaryTemplateEntity();
        entity.setTemplateName(requestDTO.getTemplateName());
        entity.setTemplateCode(StrUtil.blankToDefault(requestDTO.getTemplateCode(), "SAL-TPL-" + IdUtil.fastSimpleUUID().substring(0, 12)));
        entity.setScopeType(normalizeScopeType(requestDTO.getScopeType()));
        entity.setScopeValue(requestDTO.getScopeValue());
        entity.setStatus(Optional.ofNullable(requestDTO.getStatus()).orElse(1));
        entity.setRemark(requestDTO.getRemark());
        salaryTemplateMapper.insert(entity);
        saveTemplateItems(entity.getId(), requestDTO.getItems());
        evictTemplateCache(entity.getId());
        return toTemplateVO(entity, listTemplateItems(List.of(entity.getId())));
    }

    /**
     * 更新薪资账套。
     *
     * @param id         账套ID
     * @param requestDTO 更新请求
     * @return 更新后的薪资账套
     * 本方法使用的工具类: Wrappers(MyBatis-Plus),CollUtil(hutool),Transactional(Spring)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryTemplatePageVO updateTemplate(Long id, SalaryTemplateCreateOrUpdateRequestDTO requestDTO) {
        SalaryTemplateEntity entity = getTemplateRequired(id);
        entity.setTemplateName(requestDTO.getTemplateName());
        if (StrUtil.isNotBlank(requestDTO.getTemplateCode())) {
            entity.setTemplateCode(requestDTO.getTemplateCode());
        }
        entity.setScopeType(normalizeScopeType(requestDTO.getScopeType()));
        entity.setScopeValue(requestDTO.getScopeValue());
        entity.setStatus(Optional.ofNullable(requestDTO.getStatus()).orElse(entity.getStatus()));
        entity.setRemark(requestDTO.getRemark());
        salaryTemplateMapper.updateById(entity);
        salaryTemplateItemMapper.delete(Wrappers.lambdaQuery(SalaryTemplateItemEntity.class)
                .eq(SalaryTemplateItemEntity::getTemplateId, id));
        saveTemplateItems(id, requestDTO.getItems());
        evictTemplateCache(id);
        return toTemplateVO(entity, listTemplateItems(List.of(id)));
    }

    /**
     * 查询员工薪资档案。
     *
     * @param employeeId 员工ID
     * @return 薪资档案
     * 本方法使用的工具类: Wrappers(MyBatis-Plus)
     */
    @Override
    public EmployeeSalaryProfileVO getEmployeeProfile(Long employeeId) {
        SalaryEmployeeSnapshotEntity employee = getEmployeeRequired(employeeId);
        EmployeeSalaryProfileEntity profile = employeeSalaryProfileMapper.selectOne(Wrappers
                .lambdaQuery(EmployeeSalaryProfileEntity.class)
                .eq(EmployeeSalaryProfileEntity::getEmployeeId, employeeId));
        if (profile == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "员工薪资档案不存在");
        }
        return toProfileVO(profile, employee);
    }

    /**
     * 设置员工薪资档案。
     *
     * @param employeeId 员工ID
     * @param requestDTO 设置请求
     * @return 设置后的薪资档案
     * 本方法使用的工具类: Wrappers(MyBatis-Plus),ObjectUtil(JDK),Transactional(Spring)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeSalaryProfileVO setEmployeeProfile(Long employeeId, EmployeeSalaryProfileRequestDTO requestDTO) {
        SalaryEmployeeSnapshotEntity employee = getEmployeeRequired(employeeId);
        if (requestDTO.getTemplateId() != null) {
            getTemplateRequired(requestDTO.getTemplateId());
        }
        EmployeeSalaryProfileEntity profile = employeeSalaryProfileMapper.selectOne(Wrappers
                .lambdaQuery(EmployeeSalaryProfileEntity.class)
                .eq(EmployeeSalaryProfileEntity::getEmployeeId, employeeId));
        boolean create = profile == null;
        if (create) {
            profile = new EmployeeSalaryProfileEntity();
            profile.setEmployeeId(employeeId);
        }
        profile.setTemplateId(requestDTO.getTemplateId());
        profile.setBaseSalary(money(requestDTO.getBaseSalary()));
        profile.setAllowance(money(requestDTO.getAllowance()));
        profile.setPerformanceBase(money(requestDTO.getPerformanceBase()));
        profile.setSocialInsuranceBase(money(requestDTO.getSocialInsuranceBase()));
        profile.setHousingFundBase(money(requestDTO.getHousingFundBase()));
        profile.setBankName(requestDTO.getBankName());
        profile.setBankAccount(requestDTO.getBankAccount());
        profile.setEffectiveDate(requestDTO.getEffectiveDate());
        profile.setRemark(requestDTO.getRemark());
        if (create) {
            employeeSalaryProfileMapper.insert(profile);
        } else {
            employeeSalaryProfileMapper.updateById(profile);
        }
        return toProfileVO(profile, employee);
    }

    /**
     * 创建薪资核算批次。
     *
     * @param requestDTO 创建请求
     * @return 薪资批次
     * 本方法使用的工具类: IdUtil(hutool),StrUtil(hutool),Wrappers(MyBatis-Plus),Transactional(Spring)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryBatchVO createBatch(SalaryBatchCreateRequestDTO requestDTO) {
        String month = normalizeMonth(StrUtil.blankToDefault(requestDTO.getSalaryMonth(), requestDTO.getMonth()));
        String scopeType = normalizeScopeType(requestDTO.getScopeType());
        String scopeValue = normalizeScopeValue(scopeType, requestDTO);
        Long duplicateCount = salaryBatchMapper.selectCount(Wrappers.lambdaQuery(SalaryBatchEntity.class)
                .eq(SalaryBatchEntity::getSalaryMonth, month)
                .eq(SalaryBatchEntity::getScopeType, scopeType)
                .eq(StrUtil.isNotBlank(scopeValue), SalaryBatchEntity::getScopeValue, scopeValue)
                .notIn(SalaryBatchEntity::getBatchStatus, SalaryBatchStatusEnum.ARCHIVED.name()));
        if (duplicateCount > 0) {
            throw new GlobalException(ErrorCode.DATA_DUPLICATE, "同月份同范围薪资批次已存在");
        }
        SalaryBatchEntity batch = new SalaryBatchEntity();
        batch.setBatchNo("SAL-" + month.replace("-", "") + "-" + IdUtil.fastSimpleUUID().substring(0, 8));
        batch.setSalaryMonth(month);
        batch.setScopeType(scopeType);
        batch.setScopeValue(scopeValue);
        batch.setBatchStatus(SalaryBatchStatusEnum.DRAFT.name());
        batch.setTotalCount(0);
        batch.setTotalGrossSalary(ZERO);
        batch.setTotalNetSalary(ZERO);
        batch.setYellowWarningCount(0);
        batch.setRedWarningCount(0);
        batch.setBlockCount(0);
        salaryBatchMapper.insert(batch);
        return toBatchVO(batch);
    }

    /**
     * 触发薪资核算。
     *
     * @param batchId 批次ID
     * @return 核算后的批次
     * 本方法使用的工具类: StringRedisTemplate(spring-data-redis),RabbitTemplate(spring-amqp),IdUtil(hutool),Transactional(Spring)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryBatchVO calculateBatch(Long batchId) {
        SalaryBatchEntity batch = getBatchRequired(batchId);
        if (!SalaryBatchStatusEnum.canCalculate(batch.getBatchStatus())) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "当前批次状态不可核算");
        }
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        String lockKey = SalaryCacheKeys.calculateLock(batchId);
        Boolean locked = redisTemplate == null ? Boolean.TRUE :
                redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(locked)) {
            throw new GlobalException(ErrorCode.CONFLICT, "薪资批次正在核算中，请稍后重试");
        }
        try {
            SalaryBatchCalculateMessage message = new SalaryBatchCalculateMessage(
                    IdUtil.fastSimpleUUID(), batchId, batch.getSalaryMonth());
            publishCalculateMessage(message);
            doCalculateBatch(batchId);
            evictBatchCache(batchId);
            return toBatchVO(getBatchRequired(batchId));
        } finally {
            if (redisTemplate != null) {
                redisTemplate.delete(lockKey);
            }
        }
    }

    /**
     * 预览薪资批次。
     *
     * @param batchId 批次ID
     * @return 批次预览
     * 本方法使用的工具类: StringRedisTemplate(spring-data-redis),JSONUtil(hutool)
     */
    @Override
    public SalaryBatchPreviewVO previewBatch(Long batchId) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        String cacheKey = SalaryCacheKeys.batchPreview(batchId);
        if (redisTemplate != null) {
            String cacheValue = redisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(cacheValue)) {
                return JSONUtil.toBean(cacheValue, SalaryBatchPreviewVO.class);
            }
        }
        SalaryBatchEntity batch = getBatchRequired(batchId);
        List<SalaryBatchItemEntity> items = salaryBatchItemMapper.selectList(Wrappers
                .lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getBatchId, batchId)
                .orderByAsc(SalaryBatchItemEntity::getEmployeeId));
        Map<Long, SalaryEmployeeSnapshotEntity> employeeMap = listEmployeesByIds(
                items.stream().map(SalaryBatchItemEntity::getEmployeeId).toList());
        SalaryBatchPreviewVO vo = new SalaryBatchPreviewVO();
        vo.setBatch(toBatchVO(batch));
        vo.setItems(items.stream().map(item -> toBatchItemVO(item, employeeMap.get(item.getEmployeeId()))).toList());
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(vo), 5, TimeUnit.MINUTES);
        }
        return vo;
    }

    /**
     * 提交薪资批次审批。
     *
     * @param batchId 批次ID
     * @return 提交后的批次
     * 本方法使用的工具类: IdUtil(hutool),Transactional(Spring)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryBatchVO submitBatch(Long batchId) {
        SalaryBatchEntity batch = getBatchRequired(batchId);
        if (!SalaryBatchStatusEnum.PENDING_REVIEW.name().equals(batch.getBatchStatus())) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "只有待复核批次可以提交审批");
        }
        if (Optional.ofNullable(batch.getBlockCount()).orElse(0) > 0) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "存在阻断异常，不能提交审批");
        }
        Long approvalInstanceId = tempStartSalaryBatchApproval(batch);
        batch.setApprovalInstanceId(approvalInstanceId);
        batch.setBatchStatus(SalaryBatchStatusEnum.APPROVING.name());
        salaryBatchMapper.updateById(batch);
        evictBatchCache(batchId);
        return toBatchVO(batch);
    }

    /**
     * 查询当前员工工资条列表。
     *
     * @param month 薪资月份，可为空
     * @return 工资条列表
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),Wrappers(MyBatis-Plus),StringRedisTemplate(spring-data-redis)
     */
    @Override
    public List<SalaryPayslipListVO> listPayslips(String month) {
        Long employeeId = getCurrentEmployeeId();
        List<SalaryBatchEntity> batches = salaryBatchMapper.selectList(Wrappers.lambdaQuery(SalaryBatchEntity.class)
                .eq(StrUtil.isNotBlank(month), SalaryBatchEntity::getSalaryMonth, month)
                .in(SalaryBatchEntity::getBatchStatus, PAYSLIP_VISIBLE_STATUS)
                .orderByDesc(SalaryBatchEntity::getSalaryMonth));
        if (CollUtil.isEmpty(batches)) {
            return List.of();
        }
        Map<Long, SalaryBatchEntity> batchMap = batches.stream()
                .collect(Collectors.toMap(SalaryBatchEntity::getId, Function.identity()));
        List<SalaryBatchItemEntity> items = salaryBatchItemMapper.selectList(Wrappers
                .lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getEmployeeId, employeeId)
                .in(SalaryBatchItemEntity::getBatchId, batchMap.keySet())
                .orderByDesc(SalaryBatchItemEntity::getId));
        return items.stream().map(item -> toPayslipListVO(item, batchMap.get(item.getBatchId()))).toList();
    }

    /**
     * 工资条二次验证。
     *
     * @param requestDTO 验证请求
     * @return 验证结果
     * 本方法使用的工具类: PasswordEncoder(spring-security-crypto),StringRedisTemplate(spring-data-redis),IdUtil(hutool)
     */
    @Override
    public SalaryPayslipVerifyVO verifyPayslip(SalaryPayslipVerifyRequestDTO requestDTO) {
        Long userId = SecurityContextHolder.getUserId();
        Long employeeId = getCurrentEmployeeId();
        SalarySysUserEntity user = salarySysUserMapper.selectById(userId);
        if (user == null || Objects.equals(user.getIsDeleted(), 1) || Objects.equals(user.getStatus(), 0)) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED, "当前用户不可用");
        }
        boolean passwordOk = StrUtil.isNotBlank(requestDTO.getPassword())
                && getPasswordEncoder().matches(requestDTO.getPassword(), user.getPassword());
        boolean smsOk = StrUtil.isNotBlank(requestDTO.getSmsCode()) && tempVerifySmsCode(userId, requestDTO.getSmsCode());
        if (!passwordOk && !smsOk) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "工资条二次验证失败");
        }
        String token = IdUtil.fastSimpleUUID();
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(SalaryCacheKeys.payslipVerify(employeeId, requestDTO.getMonth()),
                    token, 30, TimeUnit.MINUTES);
        }
        SalaryPayslipVerifyVO vo = new SalaryPayslipVerifyVO();
        vo.setSuccess(true);
        vo.setToken(token);
        vo.setExpireTime(LocalDateTime.now().plusMinutes(30));
        return vo;
    }

    /**
     * 查询工资条详情。
     *
     * @param payslipId 工资条ID
     * @return 工资条详情
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),StringRedisTemplate(spring-data-redis),Wrappers(MyBatis-Plus)
     */
    @Override
    public SalaryPayslipDetailVO getPayslipDetail(Long payslipId) {
        Long employeeId = getCurrentEmployeeId();
        SalaryBatchItemEntity item = salaryBatchItemMapper.selectById(payslipId);
        if (item == null || !Objects.equals(item.getEmployeeId(), employeeId)) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "工资条不存在");
        }
        SalaryBatchEntity batch = getBatchRequired(item.getBatchId());
        if (!PAYSLIP_VISIBLE_STATUS.contains(batch.getBatchStatus())) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "工资条暂不可查看");
        }
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null && !Boolean.TRUE.equals(redisTemplate.hasKey(
                SalaryCacheKeys.payslipVerify(employeeId, batch.getSalaryMonth())))) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "请先完成工资条二次验证");
        }
        SalaryPayslipDetailVO vo = new SalaryPayslipDetailVO();
        SalaryEmployeeSnapshotEntity employee = employeeSnapshotMapper.selectById(employeeId);
        copyBatchItemFields(item, vo, employee);
        vo.setSalaryMonth(batch.getSalaryMonth());
        vo.setBatchNo(batch.getBatchNo());
        return vo;
    }

    /**
     * 查询当前员工近 6 个月薪资趋势。
     *
     * @return 薪资趋势
     * 本方法使用的工具类: YearMonth(JDK),Wrappers(MyBatis-Plus),List(JDK)
     */
    @Override
    public List<SalaryTrendVO> getTrend() {
        Long employeeId = getCurrentEmployeeId();
        String startMonth = YearMonth.now().minusMonths(5).toString();
        List<SalaryBatchEntity> batches = salaryBatchMapper.selectList(Wrappers.lambdaQuery(SalaryBatchEntity.class)
                .ge(SalaryBatchEntity::getSalaryMonth, startMonth)
                .in(SalaryBatchEntity::getBatchStatus, PAYSLIP_VISIBLE_STATUS)
                .orderByAsc(SalaryBatchEntity::getSalaryMonth));
        if (CollUtil.isEmpty(batches)) {
            return List.of();
        }
        Map<Long, SalaryBatchEntity> batchMap = batches.stream()
                .collect(Collectors.toMap(SalaryBatchEntity::getId, Function.identity()));
        List<SalaryBatchItemEntity> items = salaryBatchItemMapper.selectList(Wrappers
                .lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getEmployeeId, employeeId)
                .in(SalaryBatchItemEntity::getBatchId, batchMap.keySet()));
        return items.stream()
                .map(item -> {
                    SalaryTrendVO vo = new SalaryTrendVO();
                    vo.setMonth(batchMap.get(item.getBatchId()).getSalaryMonth());
                    vo.setNetSalary(item.getNetSalary());
                    return vo;
                })
                .sorted(Comparator.comparing(SalaryTrendVO::getMonth))
                .toList();
    }

    /**
     * 执行薪资核算。
     *
     * @param batchId 批次ID
     * 本方法使用的工具类: Wrappers(MyBatis-Plus),BigDecimal(JDK),Transactional(Spring)
     */
    @Transactional(rollbackFor = Exception.class)
    protected void doCalculateBatch(Long batchId) {
        SalaryBatchEntity batch = getBatchRequired(batchId);
        batch.setBatchStatus(SalaryBatchStatusEnum.CALCULATING.name());
        salaryBatchMapper.updateById(batch);
        salaryBatchItemMapper.delete(Wrappers.lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getBatchId, batchId));
        List<SalaryEmployeeSnapshotEntity> employees = resolveBatchEmployees(batch);
        Map<Long, EmployeeSalaryProfileEntity> profileMap = listProfiles(employees.stream()
                .map(SalaryEmployeeSnapshotEntity::getId).toList());
        Map<Long, AttendancePayrollSourceVO> attendanceMap = getAttendanceSummary(
                batch.getSalaryMonth(), employees.stream().map(SalaryEmployeeSnapshotEntity::getId).toList());
        int yellow = 0;
        int red = 0;
        int block = 0;
        BigDecimal totalGross = ZERO;
        BigDecimal totalNet = ZERO;
        for (SalaryEmployeeSnapshotEntity employee : employees) {
            SalaryBatchItemEntity item = calculateEmployeeItem(batch, employee, profileMap.get(employee.getId()),
                    attendanceMap.get(employee.getId()));
            salaryBatchItemMapper.insert(item);
            totalGross = totalGross.add(item.getGrossSalary());
            totalNet = totalNet.add(item.getNetSalary());
            if (SalaryWarningLevelEnum.YELLOW.name().equals(item.getWarningLevel())) {
                yellow++;
            } else if (SalaryWarningLevelEnum.RED.name().equals(item.getWarningLevel())) {
                red++;
            } else if (SalaryWarningLevelEnum.BLOCK.name().equals(item.getWarningLevel())) {
                block++;
            }
        }
        batch.setBatchStatus(SalaryBatchStatusEnum.PENDING_REVIEW.name());
        batch.setTotalCount(employees.size());
        batch.setTotalGrossSalary(money(totalGross));
        batch.setTotalNetSalary(money(totalNet));
        batch.setYellowWarningCount(yellow);
        batch.setRedWarningCount(red);
        batch.setBlockCount(block);
        salaryBatchMapper.updateById(batch);
    }

    private SalaryBatchItemEntity calculateEmployeeItem(SalaryBatchEntity batch,
                                                        SalaryEmployeeSnapshotEntity employee,
                                                        EmployeeSalaryProfileEntity profile,
                                                        AttendancePayrollSourceVO attendance) {
        SalaryBatchItemEntity item = new SalaryBatchItemEntity();
        item.setBatchId(batch.getId());
        item.setEmployeeId(employee.getId());
        if (profile == null) {
            fillZeroSalary(item);
            item.setWarningLevel(SalaryWarningLevelEnum.BLOCK.name());
            item.setWarningReason("缺少员工薪资档案");
            return item;
        }
        BigDecimal baseSalary = money(profile.getBaseSalary());
        BigDecimal allowance = money(profile.getAllowance());
        BigDecimal performanceBonus = money(profile.getPerformanceBase());
        BigDecimal overtimePay = money(Optional.ofNullable(attendance)
                .map(AttendancePayrollSourceVO::getOvertimeHours)
                .orElse(BigDecimal.ZERO).multiply(new BigDecimal("50")));
        BigDecimal lateDeduction = money(new BigDecimal(Optional.ofNullable(attendance)
                .map(AttendancePayrollSourceVO::getLateCount).orElse(0)).multiply(new BigDecimal("20")));
        BigDecimal leaveDays = Optional.ofNullable(attendance).map(AttendancePayrollSourceVO::getLeaveDays).orElse(BigDecimal.ZERO);
        BigDecimal leaveDeduction = money(baseSalary.divide(new BigDecimal("21.75"), 2, RoundingMode.HALF_UP).multiply(leaveDays));
        BigDecimal socialInsurance = money(profile.getSocialInsuranceBase()).multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal housingFund = money(profile.getHousingFundBase()).multiply(new BigDecimal("0.07")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gross = baseSalary.add(allowance).add(performanceBonus).add(overtimePay);
        BigDecimal incomeTax = calculateIncomeTax(gross.subtract(socialInsurance).subtract(housingFund));
        BigDecimal deduction = lateDeduction.add(leaveDeduction).add(socialInsurance).add(housingFund).add(incomeTax);
        BigDecimal net = gross.subtract(deduction).setScale(2, RoundingMode.HALF_UP);
        item.setBaseSalary(baseSalary);
        item.setAllowance(allowance);
        item.setPerformanceBonus(performanceBonus);
        item.setOvertimePay(overtimePay);
        item.setLateDeduction(lateDeduction);
        item.setLeaveDeduction(leaveDeduction);
        item.setSocialInsurance(socialInsurance);
        item.setHousingFund(housingFund);
        item.setIncomeTax(incomeTax);
        item.setGrossSalary(gross);
        item.setDeductionTotal(deduction);
        item.setNetSalary(net);
        applyWarning(batch, employee.getId(), item, leaveDays);
        return item;
    }

    private void applyWarning(SalaryBatchEntity batch, Long employeeId, SalaryBatchItemEntity item, BigDecimal leaveDays) {
        if (item.getNetSalary().compareTo(BigDecimal.ZERO) <= 0) {
            item.setWarningLevel(SalaryWarningLevelEnum.BLOCK.name());
            item.setWarningReason("实发工资小于等于 0");
            return;
        }
        if (leaveDays.compareTo(new BigDecimal("15")) > 0) {
            item.setWarningLevel(SalaryWarningLevelEnum.YELLOW.name());
            item.setWarningReason("本月请假天数超过 15 天");
            return;
        }
        BigDecimal previousNet = findPreviousNetSalary(employeeId, batch.getSalaryMonth());
        if (previousNet.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate = item.getNetSalary().subtract(previousNet).abs()
                    .divide(previousNet, 4, RoundingMode.HALF_UP);
            if (rate.compareTo(new BigDecimal("0.30")) > 0) {
                item.setWarningLevel(SalaryWarningLevelEnum.RED.name());
                item.setWarningReason("较上月实发工资波动超过 30%");
                return;
            }
        }
        item.setWarningLevel(SalaryWarningLevelEnum.NONE.name());
        item.setWarningReason(null);
    }

    private BigDecimal findPreviousNetSalary(Long employeeId, String salaryMonth) {
        String previousMonth = YearMonth.parse(salaryMonth).minusMonths(1).toString();
        List<SalaryBatchEntity> previousBatches = salaryBatchMapper.selectList(Wrappers.lambdaQuery(SalaryBatchEntity.class)
                .eq(SalaryBatchEntity::getSalaryMonth, previousMonth)
                .in(SalaryBatchEntity::getBatchStatus, PAYSLIP_VISIBLE_STATUS));
        if (CollUtil.isEmpty(previousBatches)) {
            return ZERO;
        }
        SalaryBatchItemEntity previous = salaryBatchItemMapper.selectOne(Wrappers.lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getEmployeeId, employeeId)
                .in(SalaryBatchItemEntity::getBatchId,
                        previousBatches.stream().map(SalaryBatchEntity::getId).toList())
                .last("limit 1"));
        return previous == null ? ZERO : money(previous.getNetSalary());
    }

    private void fillZeroSalary(SalaryBatchItemEntity item) {
        item.setBaseSalary(ZERO);
        item.setAllowance(ZERO);
        item.setPerformanceBonus(ZERO);
        item.setOvertimePay(ZERO);
        item.setLateDeduction(ZERO);
        item.setLeaveDeduction(ZERO);
        item.setSocialInsurance(ZERO);
        item.setHousingFund(ZERO);
        item.setIncomeTax(ZERO);
        item.setGrossSalary(ZERO);
        item.setDeductionTotal(ZERO);
        item.setNetSalary(ZERO);
    }

    private BigDecimal calculateIncomeTax(BigDecimal taxable) {
        BigDecimal threshold = new BigDecimal("5000");
        if (taxable.compareTo(threshold) <= 0) {
            return ZERO;
        }
        return taxable.subtract(threshold).multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP);
    }

    private List<SalaryEmployeeSnapshotEntity> resolveBatchEmployees(SalaryBatchEntity batch) {
        LambdaQueryWrapper<SalaryEmployeeSnapshotEntity> wrapper = Wrappers.lambdaQuery(SalaryEmployeeSnapshotEntity.class)
                .eq(SalaryEmployeeSnapshotEntity::getIsDeleted, 0)
                .ne(SalaryEmployeeSnapshotEntity::getEmploymentStatus, 4);
        if ("DEPT".equals(batch.getScopeType()) && StrUtil.isNotBlank(batch.getScopeValue())) {
            wrapper.in(SalaryEmployeeSnapshotEntity::getDeptId, parseLongList(batch.getScopeValue()));
        } else if ("EMPLOYEE".equals(batch.getScopeType()) && StrUtil.isNotBlank(batch.getScopeValue())) {
            wrapper.in(SalaryEmployeeSnapshotEntity::getId, parseLongList(batch.getScopeValue()));
        }
        return employeeSnapshotMapper.selectList(wrapper);
    }

    private Map<Long, EmployeeSalaryProfileEntity> listProfiles(List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Map.of();
        }
        return employeeSalaryProfileMapper.selectList(Wrappers.lambdaQuery(EmployeeSalaryProfileEntity.class)
                        .in(EmployeeSalaryProfileEntity::getEmployeeId, employeeIds))
                .stream().collect(Collectors.toMap(EmployeeSalaryProfileEntity::getEmployeeId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, AttendancePayrollSourceVO> getAttendanceSummary(String month, List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Map.of();
        }
        AttendanceService attendanceService = attendanceServiceProvider.getIfAvailable();
        List<AttendancePayrollSourceVO> summary;
        if (attendanceService != null) {
            summary = attendanceService.getPayrollSource(month, employeeIds);
        } else {
            summary = tempGetAttendanceMonthlySummary(month, employeeIds);
        }
        return summary.stream().collect(Collectors.toMap(AttendancePayrollSourceVO::getEmployeeId, Function.identity(), (a, b) -> a));
    }

    private List<AttendancePayrollSourceVO> tempGetAttendanceMonthlySummary(String month, List<Long> employeeIds) {
        // 本方法未来替换为 hrms-business-attendance 的 AttendanceService#getPayrollSource(month, employeeIds)。
        return employeeIds.stream().map(employeeId -> {
            AttendancePayrollSourceVO vo = new AttendancePayrollSourceVO();
            vo.setEmployeeId(employeeId);
            vo.setShouldAttendDays(22);
            vo.setActualAttendDays(22);
            vo.setLateCount(0);
            vo.setEarlyLeaveCount(0);
            vo.setAbsenceDays(BigDecimal.ZERO);
            vo.setLeaveDays(BigDecimal.ZERO);
            vo.setOvertimeHours(BigDecimal.ZERO);
            return vo;
        }).toList();
    }

    private void publishCalculateMessage(SalaryBatchCalculateMessage message) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            tempPublishCalculateMessage(message);
            return;
        }
        try {
            rabbitTemplate.convertAndSend(SalaryMqConstants.SALARY_EXCHANGE,
                    SalaryMqConstants.BATCH_CALCULATE_ROUTING_KEY, JSONUtil.toJsonStr(message));
        } catch (Exception ex) {
            log.warn("publish salary.batch.calculate failed, use temp publisher, message={}", JSONUtil.toJsonStr(message), ex);
            tempPublishCalculateMessage(message);
        }
    }

    private void tempPublishCalculateMessage(SalaryBatchCalculateMessage message) {
        // 本方法未来替换为 salary.batch.calculate Producer，用于异步触发薪资批次核算。
        log.info("temp salary.batch.calculate message: {}", JSONUtil.toJsonStr(message));
    }

    private Long tempStartSalaryBatchApproval(SalaryBatchEntity batch) {
        // 本方法未来替换为 hrms-business-approval 的薪资批次审批发起接口。
        return Math.abs(IdUtil.getSnowflakeNextId());
    }

    private boolean tempVerifySmsCode(Long userId, String smsCode) {
        // 本方法未来替换为 auth/notification 模块短信验证码校验接口。
        return "123456".equals(smsCode);
    }

    private void saveTemplateItems(Long templateId, List<SalaryTemplateItemRequestDTO> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (SalaryTemplateItemRequestDTO itemDTO : items) {
            SalaryTemplateItemEntity item = new SalaryTemplateItemEntity();
            item.setTemplateId(templateId);
            item.setItemCode(itemDTO.getItemCode());
            item.setItemName(itemDTO.getItemName());
            item.setCategory(itemDTO.getCategory());
            item.setCalcRule(itemDTO.getCalcRule());
            item.setDefaultValue(money(itemDTO.getDefaultValue()));
            item.setSortNo(Optional.ofNullable(itemDTO.getSortNo()).orElse(0));
            salaryTemplateItemMapper.insert(item);
        }
    }

    private List<SalaryTemplateItemEntity> listTemplateItems(List<Long> templateIds) {
        if (CollUtil.isEmpty(templateIds)) {
            return List.of();
        }
        return salaryTemplateItemMapper.selectList(Wrappers.lambdaQuery(SalaryTemplateItemEntity.class)
                .in(SalaryTemplateItemEntity::getTemplateId, templateIds)
                .orderByAsc(SalaryTemplateItemEntity::getSortNo));
    }

    private SalaryTemplateEntity getTemplateRequired(Long id) {
        SalaryTemplateEntity entity = salaryTemplateMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "薪资账套不存在");
        }
        return entity;
    }

    private SalaryBatchEntity getBatchRequired(Long id) {
        SalaryBatchEntity entity = salaryBatchMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "薪资批次不存在");
        }
        return entity;
    }

    private SalaryEmployeeSnapshotEntity getEmployeeRequired(Long employeeId) {
        SalaryEmployeeSnapshotEntity employee = employeeSnapshotMapper.selectById(employeeId);
        if (employee == null || Objects.equals(employee.getIsDeleted(), 1)) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "员工不存在");
        }
        return employee;
    }

    private Long getCurrentEmployeeId() {
        Long userId = SecurityContextHolder.getUserId();
        SalaryEmployeeSnapshotEntity employee = employeeSnapshotMapper.selectOne(Wrappers
                .lambdaQuery(SalaryEmployeeSnapshotEntity.class)
                .eq(SalaryEmployeeSnapshotEntity::getUserId, userId)
                .eq(SalaryEmployeeSnapshotEntity::getIsDeleted, 0)
                .last("limit 1"));
        if (employee != null) {
            return employee.getId();
        }
        SalarySysUserEntity user = salarySysUserMapper.selectById(userId);
        if (user != null && user.getEmployeeId() != null) {
            return user.getEmployeeId();
        }
        throw new GlobalException(ErrorCode.NOT_FOUND, "当前登录用户未绑定员工档案");
    }

    private Map<Long, SalaryEmployeeSnapshotEntity> listEmployeesByIds(List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Map.of();
        }
        return employeeSnapshotMapper.selectList(Wrappers.lambdaQuery(SalaryEmployeeSnapshotEntity.class)
                        .in(SalaryEmployeeSnapshotEntity::getId, employeeIds))
                .stream().collect(Collectors.toMap(SalaryEmployeeSnapshotEntity::getId, Function.identity(), (a, b) -> a));
    }

    private EmployeeSalaryProfileVO toProfileVO(EmployeeSalaryProfileEntity profile, SalaryEmployeeSnapshotEntity employee) {
        EmployeeSalaryProfileVO vo = new EmployeeSalaryProfileVO();
        vo.setId(profile.getId());
        vo.setEmployeeId(profile.getEmployeeId());
        vo.setEmployeeNo(employee.getEmployeeNo());
        vo.setEmployeeName(employee.getEmployeeName());
        vo.setTemplateId(profile.getTemplateId());
        if (profile.getTemplateId() != null) {
            SalaryTemplateEntity template = salaryTemplateMapper.selectById(profile.getTemplateId());
            vo.setTemplateName(template == null ? null : template.getTemplateName());
        }
        vo.setBaseSalary(profile.getBaseSalary());
        vo.setAllowance(profile.getAllowance());
        vo.setPerformanceBase(profile.getPerformanceBase());
        vo.setSocialInsuranceBase(profile.getSocialInsuranceBase());
        vo.setHousingFundBase(profile.getHousingFundBase());
        vo.setBankName(profile.getBankName());
        vo.setBankAccountMasked(maskBankAccount(profile.getBankAccount()));
        vo.setEffectiveDate(profile.getEffectiveDate());
        vo.setRemark(profile.getRemark());
        return vo;
    }

    private SalaryTemplatePageVO toTemplateVO(SalaryTemplateEntity entity, List<SalaryTemplateItemEntity> items) {
        SalaryTemplatePageVO vo = new SalaryTemplatePageVO();
        vo.setId(entity.getId());
        vo.setTemplateName(entity.getTemplateName());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setScopeType(entity.getScopeType());
        vo.setScopeValue(entity.getScopeValue());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setItemCount(items.size());
        vo.setItems(items.stream().map(this::toTemplateItemVO).toList());
        return vo;
    }

    private SalaryTemplateItemVO toTemplateItemVO(SalaryTemplateItemEntity entity) {
        SalaryTemplateItemVO vo = new SalaryTemplateItemVO();
        vo.setId(entity.getId());
        vo.setItemCode(entity.getItemCode());
        vo.setItemName(entity.getItemName());
        vo.setCategory(entity.getCategory());
        vo.setCalcRule(entity.getCalcRule());
        vo.setDefaultValue(entity.getDefaultValue());
        vo.setSortNo(entity.getSortNo());
        return vo;
    }

    private SalaryBatchVO toBatchVO(SalaryBatchEntity entity) {
        SalaryBatchVO vo = new SalaryBatchVO();
        vo.setId(entity.getId());
        vo.setBatchNo(entity.getBatchNo());
        vo.setSalaryMonth(entity.getSalaryMonth());
        vo.setScopeType(entity.getScopeType());
        vo.setScopeValue(entity.getScopeValue());
        vo.setBatchStatus(entity.getBatchStatus());
        vo.setApprovalInstanceId(entity.getApprovalInstanceId());
        vo.setTotalCount(entity.getTotalCount());
        vo.setTotalGrossSalary(entity.getTotalGrossSalary());
        vo.setTotalNetSalary(entity.getTotalNetSalary());
        vo.setYellowWarningCount(entity.getYellowWarningCount());
        vo.setRedWarningCount(entity.getRedWarningCount());
        vo.setBlockCount(entity.getBlockCount());
        return vo;
    }

    private SalaryBatchItemVO toBatchItemVO(SalaryBatchItemEntity item, SalaryEmployeeSnapshotEntity employee) {
        SalaryBatchItemVO vo = new SalaryBatchItemVO();
        copyBatchItemFields(item, vo, employee);
        return vo;
    }

    private void copyBatchItemFields(SalaryBatchItemEntity item, SalaryBatchItemVO vo, SalaryEmployeeSnapshotEntity employee) {
        vo.setId(item.getId());
        vo.setBatchId(item.getBatchId());
        vo.setEmployeeId(item.getEmployeeId());
        vo.setEmployeeNo(employee == null ? null : employee.getEmployeeNo());
        vo.setEmployeeName(employee == null ? null : employee.getEmployeeName());
        vo.setBaseSalary(item.getBaseSalary());
        vo.setAllowance(item.getAllowance());
        vo.setPerformanceBonus(item.getPerformanceBonus());
        vo.setOvertimePay(item.getOvertimePay());
        vo.setLateDeduction(item.getLateDeduction());
        vo.setLeaveDeduction(item.getLeaveDeduction());
        vo.setSocialInsurance(item.getSocialInsurance());
        vo.setHousingFund(item.getHousingFund());
        vo.setIncomeTax(item.getIncomeTax());
        vo.setGrossSalary(item.getGrossSalary());
        vo.setDeductionTotal(item.getDeductionTotal());
        vo.setNetSalary(item.getNetSalary());
        vo.setWarningLevel(item.getWarningLevel());
        vo.setWarningReason(item.getWarningReason());
    }

    private SalaryPayslipListVO toPayslipListVO(SalaryBatchItemEntity item, SalaryBatchEntity batch) {
        SalaryPayslipListVO vo = new SalaryPayslipListVO();
        vo.setId(item.getId());
        vo.setSalaryMonth(batch.getSalaryMonth());
        vo.setGrossSalary(item.getGrossSalary());
        vo.setDeductionTotal(item.getDeductionTotal());
        vo.setNetSalary(item.getNetSalary());
        vo.setBatchStatus(batch.getBatchStatus());
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        vo.setVerified(redisTemplate != null && Boolean.TRUE.equals(redisTemplate.hasKey(
                SalaryCacheKeys.payslipVerify(item.getEmployeeId(), batch.getSalaryMonth()))));
        return vo;
    }

    private String normalizeScopeType(String scopeType) {
        if (StrUtil.isBlank(scopeType)) {
            return "ALL";
        }
        String normalized = scopeType.trim().toUpperCase();
        if ("DEPARTMENT".equals(normalized)) {
            return "DEPT";
        }
        if (!Set.of("ALL", "DEPT", "EMPLOYEE").contains(normalized)) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "范围类型仅支持 ALL/DEPT/EMPLOYEE");
        }
        return normalized;
    }

    private String normalizeScopeValue(String scopeType, SalaryBatchCreateRequestDTO requestDTO) {
        if ("EMPLOYEE".equals(scopeType) && CollUtil.isNotEmpty(requestDTO.getEmployeeIds())) {
            return requestDTO.getEmployeeIds().stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        if (StrUtil.isNotBlank(requestDTO.getScopeValue())) {
            return requestDTO.getScopeValue();
        }
        if ("ALL".equals(scopeType)) {
            return null;
        }
        throw new GlobalException(ErrorCode.PARAM_REQUIRED, "非 ALL 范围必须传入 scopeValue 或 employeeIds");
    }

    private String normalizeMonth(String month) {
        if (StrUtil.isBlank(month)) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "薪资月份不能为空");
        }
        return YearMonth.parse(month).toString();
    }

    private List<Long> parseLongList(String value) {
        if (StrUtil.isBlank(value)) {
            return List.of();
        }
        List<Long> values = new ArrayList<>();
        for (String part : value.split(",")) {
            if (StrUtil.isNotBlank(part)) {
                values.add(Long.valueOf(part.trim()));
            }
        }
        return values;
    }

    private BigDecimal money(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private String maskBankAccount(String bankAccount) {
        if (StrUtil.isBlank(bankAccount)) {
            return null;
        }
        if (bankAccount.length() <= 8) {
            return "****" + bankAccount.substring(Math.max(0, bankAccount.length() - 4));
        }
        return bankAccount.substring(0, 4) + " **** **** " + bankAccount.substring(bankAccount.length() - 4);
    }

    private void evictTemplateCache(Long templateId) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.delete(List.of(SalaryCacheKeys.template(templateId), SalaryCacheKeys.templateItems(templateId)));
        }
    }

    private void evictBatchCache(Long batchId) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.delete(SalaryCacheKeys.batchPreview(batchId));
        }
    }

    private PasswordEncoder getPasswordEncoder() {
        return Optional.ofNullable(passwordEncoderProvider.getIfAvailable()).orElseGet(BCryptPasswordEncoder::new);
    }
}
