package com.hrms.business.salary.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendancePayrollSourceVO;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.salary.cache.SalaryCacheKeys;
import com.hrms.business.salary.dto.SalaryBatchAdjustmentRequestDTO;
import com.hrms.business.salary.dto.SalaryBatchCreateRequestDTO;
import com.hrms.business.salary.entity.EmployeeSalaryProfileEntity;
import com.hrms.business.salary.entity.SalaryBatchAdjustmentEntity;
import com.hrms.business.salary.entity.SalaryBatchEntity;
import com.hrms.business.salary.entity.SalaryBatchItemEntity;
import com.hrms.business.salary.entity.SalaryEmployeeSnapshotEntity;
import com.hrms.business.salary.enums.SalaryBatchStatusEnum;
import com.hrms.business.salary.enums.SalaryWarningLevelEnum;
import com.hrms.business.salary.mapper.EmployeeSalaryProfileMapper;
import com.hrms.business.salary.mapper.SalaryBatchAdjustmentMapper;
import com.hrms.business.salary.mapper.SalaryBatchItemMapper;
import com.hrms.business.salary.mapper.SalaryBatchMapper;
import com.hrms.business.salary.mapper.SalaryEmployeeSnapshotMapper;
import com.hrms.business.salary.mq.event.SalaryBatchCalculateMessage;
import com.hrms.business.salary.mq.producer.SalaryBatchCalculateProducer;
import com.hrms.business.salary.service.SalaryCalculateService;
import com.hrms.business.salary.vo.SalaryBatchExportVO;
import com.hrms.business.salary.vo.SalaryBatchItemVO;
import com.hrms.business.salary.vo.SalaryBatchPreviewVO;
import com.hrms.business.salary.vo.SalaryBatchTrendVO;
import com.hrms.business.salary.vo.SalaryBatchVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.file.config.FileConfig;
import com.hrms.system.file.service.FileService;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.vo.DeptDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 薪资核算服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryCalculateServiceImpl implements SalaryCalculateService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal DEFAULT_PENSION_INSURANCE_RATE = new BigDecimal("0.0800");
    private static final BigDecimal DEFAULT_MEDICAL_INSURANCE_RATE = new BigDecimal("0.0200");
    private static final BigDecimal DEFAULT_UNEMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.0050");
    // 薪资条可见状态
    private static final Set<String> PAYSLIP_VISIBLE_STATUS = Set.of(
            SalaryBatchStatusEnum.APPROVING.name(),
            SalaryBatchStatusEnum.APPROVED.name(),
            SalaryBatchStatusEnum.RELEASED.name(),
            SalaryBatchStatusEnum.ARCHIVED.name()
    );
    // 薪资批次导出允许状态
    private static final Set<String> BATCH_EXPORT_ALLOWED_STATUS = Set.of(
            SalaryBatchStatusEnum.APPROVED.name(),
            SalaryBatchStatusEnum.RELEASED.name()
    );
    // 薪资管理员角色码
    private static final Set<String> SALARY_MANAGER_ROLE_CODES = Set.of(
            "FINANCE", "HR", "HR_TEST", "ADMIN", "ROLE_ADMIN"
    );
    // 薪资调整项码
    private static final Set<String> SALARY_ADJUST_ITEM_CODES = Set.of(
            "BASE_SALARY",
            "ALLOWANCE",
            "PERFORMANCE_BONUS",
            "OVERTIME_PAY",
            "LATE_DEDUCTION",
            "LEAVE_DEDUCTION",
            "SOCIAL_INSURANCE",
            "PENSION_INSURANCE",
            "MEDICAL_INSURANCE",
            "UNEMPLOYMENT_INSURANCE",
            "HOUSING_FUND",
            "INCOME_TAX"
    );
    private static final String SALARY_EXPORT_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String SALARY_EXPORT_FILE_TYPE = "xlsx";
    private static final String SALARY_EXPORT_BUSINESS_TYPE = "SALARY_BATCH_EXPORT";

    private final SalaryBatchMapper salaryBatchMapper;
    private final SalaryBatchAdjustmentMapper salaryBatchAdjustmentMapper;
    private final SalaryBatchItemMapper salaryBatchItemMapper;
    private final SalaryEmployeeSnapshotMapper employeeSnapshotMapper;
    private final EmployeeSalaryProfileMapper employeeSalaryProfileMapper;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ObjectProvider<AttendanceService> attendanceServiceProvider;
    private final SalaryBatchCalculateProducer salaryBatchCalculateProducer;
    private final ApprovalEngine approvalEngine;
    private final RoleService roleService;
    private final DeptService deptService;
    private final FileService fileService;
    private final FileConfig fileConfig;

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
        // 校验薪资批次是否重复
        String scopeType = normalizeScopeType(requestDTO.getScopeType());
        String scopeValue = normalizeScopeValue(scopeType, requestDTO);

        Long duplicateCount = salaryBatchMapper.selectCount(Wrappers.lambdaQuery(SalaryBatchEntity.class)
                .eq(SalaryBatchEntity::getSalaryMonth, month)// 薪资月份
                .eq(SalaryBatchEntity::getScopeType, scopeType)// 薪资范围类型
                .eq(StrUtil.isNotBlank(scopeValue), SalaryBatchEntity::getScopeValue, scopeValue)// 薪资范围值
                .notIn(SalaryBatchEntity::getBatchStatus, SalaryBatchStatusEnum.ARCHIVED.name()));// 薪资状态
        if (duplicateCount > 0) {
            throw new GlobalException(ErrorCode.DATA_DUPLICATE, "同月份同范围薪资批次已存在");
        }
        SalaryBatchEntity batch = SalaryBatchEntity.builder()
                .batchNo("SAL-" + month.replace("-", "") + "-" + IdUtil.fastSimpleUUID().substring(0, 8))
                .salaryMonth(month)
                .scopeType(scopeType)
                .scopeValue(scopeValue)
                .batchStatus(SalaryBatchStatusEnum.DRAFT.name())
                .totalCount(0)
                .totalGrossSalary(ZERO)
                .totalNetSalary(ZERO)
                .yellowWarningCount(0)
                .redWarningCount(0)
                .blockCount(0)
                .build();
        salaryBatchMapper.insert(batch);
        return toBatchVO(batch);
    }

    /**
     * 按月份和核算范围查询当前薪资批次。
     *
     * @param salaryMonth 薪资月份
     * @param scopeType   核算范围类型
     * @param scopeValue  核算范围值
     * @return 当前薪资批次，未找到时返回null
     * 本方法使用的工具类: StrUtil(hutool),Wrappers(MyBatis-Plus)
     */
    @Override
    public SalaryBatchVO getCurrentBatch(String salaryMonth, String scopeType, String scopeValue) {
        assertSalaryManagerRole();
        String month = normalizeMonth(salaryMonth);
        String normalizedScopeType = normalizeScopeType(scopeType);
        String normalizedScopeValue = normalizeBatchScopeValue(normalizedScopeType, scopeValue);
        SalaryBatchEntity batch = salaryBatchMapper.selectOne(Wrappers.lambdaQuery(SalaryBatchEntity.class)
                .eq(SalaryBatchEntity::getSalaryMonth, month)
                .eq(SalaryBatchEntity::getScopeType, normalizedScopeType)
                .eq(StrUtil.isNotBlank(normalizedScopeValue), SalaryBatchEntity::getScopeValue, normalizedScopeValue)
                .notIn(SalaryBatchEntity::getBatchStatus, SalaryBatchStatusEnum.ARCHIVED.name())
                .orderByDesc(SalaryBatchEntity::getCreateTime)
                .orderByDesc(SalaryBatchEntity::getId)
                .last("LIMIT 1"));

        // 若存在则转换为VO
        return batch == null ? null : toBatchVO(batch);
    }

    /**
     * 查询管理端跨月份薪资趋势。
     *
     * @param anchorMonth 统计截止月份
     * @param months      向前统计月数
     * @param scopeType   核算范围类型
     * @param scopeValue  核算范围值
     * @return 薪资趋势列表
     * 本方法使用的工具类: YearMonth(JDK),Wrappers(MyBatis-Plus),Collectors(JDK)
     */
    @Override
    public List<SalaryBatchTrendVO> listBatchTrend(String anchorMonth, Integer months, String scopeType, String scopeValue) {
        assertSalaryManagerRole();
        //YearMonth.parse() 方法解析字符串为标准格式（如 "2024-01"）
        YearMonth endMonth = YearMonth.parse(normalizeMonth(anchorMonth));
        int monthCount = normalizeTrendMonths(months);
        YearMonth startMonth = endMonth.minusMonths(monthCount - 1L);
        String normalizedScopeType = normalizeScopeType(scopeType);
        String normalizedScopeValue = normalizeBatchScopeValue(normalizedScopeType, scopeValue);
        List<SalaryBatchEntity> batches = salaryBatchMapper.selectList(Wrappers.lambdaQuery(SalaryBatchEntity.class)
                .ge(SalaryBatchEntity::getSalaryMonth, startMonth.toString())
                .le(SalaryBatchEntity::getSalaryMonth, endMonth.toString())
                .eq(SalaryBatchEntity::getScopeType, normalizedScopeType)
                .eq(StrUtil.isNotBlank(normalizedScopeValue), SalaryBatchEntity::getScopeValue, normalizedScopeValue)
                .notIn(SalaryBatchEntity::getBatchStatus, SalaryBatchStatusEnum.ARCHIVED.name()));
        Map<String, List<SalaryBatchEntity>> monthBatchMap = batches.stream()
                .collect(Collectors.groupingBy(SalaryBatchEntity::getSalaryMonth));
        List<SalaryBatchTrendVO> trends = new ArrayList<>();
        for (int index = 0; index < monthCount; index++) {
            String trendMonth = startMonth.plusMonths(index).toString();
            trends.add(buildBatchTrend(trendMonth, monthBatchMap.getOrDefault(trendMonth, List.of())));
        }
        return trends;
    }

    /**
     * 保存薪资批次人工调整。
     *
     * @param batchId    薪资批次ID
     * @param requestDTO 人工调整请求
     * @return 调整后的员工薪资明细
     * 本方法使用的工具类: Wrappers(MyBatis-Plus),Transactional(Spring)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryBatchItemVO saveBatchAdjustments(Long batchId, SalaryBatchAdjustmentRequestDTO requestDTO) {
        assertSalaryManagerRole();
        SalaryBatchEntity batch = getBatchRequired(batchId);
        if (!SalaryBatchStatusEnum.PENDING_REVIEW.name().equals(batch.getBatchStatus())) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "只有待复核批次可以保存人工调整");
        }
        SalaryBatchItemEntity item = salaryBatchItemMapper.selectOne(Wrappers.lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getBatchId, batchId)
                .eq(SalaryBatchItemEntity::getEmployeeId, requestDTO.getEmployeeId())
                .last("LIMIT 1"));
        if (item == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "批次员工薪资明细不存在");
        }
        List<String> itemCodes = requestDTO.getAdjustments().stream()
                .map(adjustment -> normalizeAdjustmentItemCode(adjustment.getItemCode()))
                .distinct()
                .toList();
        // 查询已有的调整记录
        List<SalaryBatchAdjustmentEntity> existingAdjustments = salaryBatchAdjustmentMapper.selectList(
                Wrappers.lambdaQuery(SalaryBatchAdjustmentEntity.class)
                        .eq(SalaryBatchAdjustmentEntity::getBatchId, batchId)
                        .eq(SalaryBatchAdjustmentEntity::getEmployeeId, requestDTO.getEmployeeId())
                        .in(SalaryBatchAdjustmentEntity::getItemCode, itemCodes));
        for (SalaryBatchAdjustmentEntity existingAdjustment : existingAdjustments) {
            applyAdjustmentToItem(item, existingAdjustment.getItemCode(), money(existingAdjustment.getAdjustAmount()).negate());
        }
        // 删除已有的调整记录
        salaryBatchAdjustmentMapper.delete(Wrappers.lambdaQuery(SalaryBatchAdjustmentEntity.class)
                .eq(SalaryBatchAdjustmentEntity::getBatchId, batchId)
                .eq(SalaryBatchAdjustmentEntity::getEmployeeId, requestDTO.getEmployeeId())
                .in(SalaryBatchAdjustmentEntity::getItemCode, itemCodes));
        // 插入新的调整记录
        for (SalaryBatchAdjustmentRequestDTO.AdjustmentItem adjustment : requestDTO.getAdjustments()) {
            SalaryBatchAdjustmentEntity entity = SalaryBatchAdjustmentEntity.builder()
                    .batchId(batchId)
                    .employeeId(requestDTO.getEmployeeId())
                    .itemCode(normalizeAdjustmentItemCode(adjustment.getItemCode()))
                    .adjustAmount(money(adjustment.getAdjustAmount()))
                    .reason(adjustment.getReason())
                    .build();

            salaryBatchAdjustmentMapper.insert(entity);
            applyAdjustmentToItem(item, entity.getItemCode(), entity.getAdjustAmount());
        }
        // 重新计算员工薪资明细金额
        recalculateItemAmount(item);
        salaryBatchItemMapper.updateById(item);
        // 刷新薪资批次摘要
        refreshBatchSummary(batchId);
        evictBatchCache(batchId);
        SalaryEmployeeSnapshotEntity employee = employeeSnapshotMapper.selectById(item.getEmployeeId());
        return toBatchItemVO(item, employee);
    }

    /**
     * 重新计算薪资批次并应用人工调整。
     *
     * @param batchId 薪资批次ID
     * @return 重新计算后的薪资批次
     * 本方法使用的工具类: StringRedisTemplate(spring-data-redis),Transactional(Spring)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryBatchVO recalculateBatch(Long batchId) {
        assertSalaryManagerRole();
        SalaryBatchEntity batch = getBatchRequired(batchId);
        if (!Set.of(SalaryBatchStatusEnum.DRAFT.name(), SalaryBatchStatusEnum.PENDING_REVIEW.name())
                .contains(batch.getBatchStatus())) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "只有草稿或待复核批次可以重新计算");
        }
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        // 尝试获取锁
        String lockKey = SalaryCacheKeys.calculateLock(batchId);
        Boolean locked = redisTemplate == null ? Boolean.TRUE :
                redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.MINUTES);
        // 如果获取锁失败则抛出异常
        if (!Boolean.TRUE.equals(locked)) {
            throw new GlobalException(ErrorCode.CONFLICT, "薪资批次正在核算中，请稍后重试");
        }
        try {
            doCalculateBatch(batchId);// 触发薪资核算
            applyBatchAdjustments(batchId);// 应用人工调整
            evictBatchCache(batchId);// 刷新薪资批次缓存
            previewBatch(batchId);// 预览薪资批次
            return toBatchVO(getBatchRequired(batchId));
        } finally {
            if (redisTemplate != null) {
                redisTemplate.delete(lockKey);
            }
        }
    }

    /**
     * 触发薪资核算。
     *
     * @param batchId 批次ID
     * @return 已进入核算中的批次
     * 本方法使用的工具类: StringRedisTemplate(spring-data-redis),SalaryBatchCalculateProducer(本模块mq包),IdUtil(hutool),Transactional(Spring)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryBatchVO calculateBatch(Long batchId) {
        SalaryBatchEntity batch = getBatchRequired(batchId);
        // 检查批次状态是否可核算
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
        String originalStatus = batch.getBatchStatus();
        try {
            batch.setBatchStatus(SalaryBatchStatusEnum.CALCULATING.name());
            salaryBatchMapper.updateById(batch);
            SalaryBatchCalculateMessage message = SalaryBatchCalculateMessage.builder()
                    .messageId(IdUtil.fastSimpleUUID())
                    .batchId(batchId)
                    .salaryMonth(batch.getSalaryMonth())
                    .build();
            salaryBatchCalculateProducer.send(message);
            /*
            doCalculateBatch(batchId);
            evictBatchCache(batchId);
            return toBatchVO(getBatchRequired(batchId));
            */
            evictBatchCache(batchId);
            return toBatchVO(batch);
        } catch (Exception ex) {
            batch.setBatchStatus(originalStatus);
            salaryBatchMapper.updateById(batch);
            if (redisTemplate != null) {
                redisTemplate.delete(lockKey);
            }
            if (ex instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new GlobalException(ErrorCode.SYSTEM_ERROR, "发送薪资批次核算消息失败");
        }
    }

    /**
     * 处理薪资批次核算消息。
     *
     * @param message 薪资批次核算消息
     * 本方法使用的工具类: StringRedisTemplate(spring-data-redis)
     */
    @Override
    public void handleBatchCalculateMessage(SalaryBatchCalculateMessage message) {
        try {
            doCalculateBatch(message.getBatchId());
            evictBatchCache(message.getBatchId());
        } finally {
            StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
            if (redisTemplate != null) {
                redisTemplate.delete(SalaryCacheKeys.calculateLock(message.getBatchId()));
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
        // 尝试从缓存中获取批次预览
        String cacheKey = SalaryCacheKeys.batchPreview(batchId);
        if (redisTemplate != null) {
            String cacheValue = redisTemplate.opsForValue().get(cacheKey);
            // 如果缓存中存在则返回 JSONUtil.toBean的作用是将json字符串转换为SalaryBatchPreviewVO对象
            if (StrUtil.isNotBlank(cacheValue)) {
                return JSONUtil.toBean(cacheValue, SalaryBatchPreviewVO.class);
            }
        }
        SalaryBatchEntity batch = getBatchRequired(batchId);
        // 获取批次下的所有员工薪资明细
        List<SalaryBatchItemEntity> items = salaryBatchItemMapper.selectList(Wrappers
                .lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getBatchId, batchId)
                .orderByAsc(SalaryBatchItemEntity::getEmployeeId));
        Map<Long, SalaryEmployeeSnapshotEntity> employeeMap = listEmployeesByIds(
                items.stream().map(SalaryBatchItemEntity::getEmployeeId).toList());
        SalaryBatchPreviewVO vo = SalaryBatchPreviewVO.builder()
                .batch(toBatchVO(batch))
                .items(items.stream().map(item -> toBatchItemVO(item, employeeMap.get(item.getEmployeeId()))).toList())
                .build();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(vo), 5, TimeUnit.MINUTES);
        }
        return vo;
    }

    /**
     * 导出薪资批次。
     * @param batchId 批次 ID
     * @return 导出的薪资批次excel
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryBatchExportVO exportBatch(Long batchId) {
        assertSalaryManagerRole();
        SalaryBatchEntity batch = getBatchRequired(batchId);
        // 检查批次状态是否可导出
        if (!BATCH_EXPORT_ALLOWED_STATUS.contains(batch.getBatchStatus())) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "仅已通过或已发放批次支持导出");
        }
        List<SalaryBatchItemEntity> items = salaryBatchItemMapper.selectList(Wrappers
                .lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getBatchId, batchId)
                .orderByAsc(SalaryBatchItemEntity::getEmployeeId));
        // 检查批次下是否存在薪资明细,CollUtil.isEmpty: 判断集合是否为空
        if (CollUtil.isEmpty(items)) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "当前批次暂无可导出薪资明细");
        }
        Map<Long, SalaryEmployeeSnapshotEntity> employeeMap = listEmployeesByIds(
                items.stream().map(SalaryBatchItemEntity::getEmployeeId).toList());
        List<SalaryBatchItemVO> exportItems = items.stream()
                .map(item -> toBatchItemVO(item, employeeMap.get(item.getEmployeeId())))
                .toList();
        String fileName = buildSalaryExportFileName(batch);
        Path filePath = buildSalaryExportPath(batch);
        writeSalaryBatchWorkbook(filePath, exportItems);// 写入薪资批次数据到excel文件
        Long fileId = fileService.upload(
                fileName,
                filePath.toString(),
                resolveFileSize(filePath),
                SALARY_EXPORT_FILE_TYPE,
                SALARY_EXPORT_MIME_TYPE,
                SALARY_EXPORT_BUSINESS_TYPE,
                batchId
        );
        return SalaryBatchExportVO.builder()
                .fileId(fileId)
                .fileName(fileName)
                .downloadUrl("/api/v1/files/" + fileId + "/download")
                .build();
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
        // 跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起薪资批次审批。
        Long approvalInstanceId = approvalEngine.startApproval(
                ApprovalTypeEnum.SALARY.getCode(),
                batch.getId(),
                JSONUtil.toJsonStr(batch),
                SecurityContextHolder.getUserId(),
                null,
                null
        );
        batch.setApprovalInstanceId(approvalInstanceId);
        batch.setBatchStatus(SalaryBatchStatusEnum.APPROVING.name());
        salaryBatchMapper.updateById(batch);
        evictBatchCache(batchId);
        return toBatchVO(batch);
    }

    /**
     * 执行薪资核算。
     *
     * @param batchId 批次ID
     * 本方法使用的工具类: Wrappers(MyBatis-Plus),BigDecimal(JDK),Transactional(Spring)
     */
    @Transactional(rollbackFor = Exception.class)
    public void doCalculateBatch(Long batchId) {
        SalaryBatchEntity batch = getBatchRequired(batchId);
        batch.setBatchStatus(SalaryBatchStatusEnum.CALCULATING.name());// 设置批次状态为核算中
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
        //Lombok的Builder模式主要用于创建新对象，而不是修改现有对象
        batch.setBatchStatus(SalaryBatchStatusEnum.PENDING_REVIEW.name());
        batch.setTotalCount(employees.size());
        batch.setTotalGrossSalary(money(totalGross));
        batch.setTotalNetSalary(money(totalNet));
        batch.setYellowWarningCount(yellow);
        batch.setRedWarningCount(red);
        batch.setBlockCount(block);
        salaryBatchMapper.updateById(batch);
    }

    /**
     * 计算员工薪资项。
     * @param batch 批次
     * @param employee 员工
     * @param profile 员工薪资档案
     * @param attendance 考勤数据
     * @return 薪资项
     */
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
        // 计算各项薪资
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
        BigDecimal pensionInsurance = calculateInsuranceAmount(
                profile.getPensionInsuranceBase(), profile.getPensionInsuranceRate(),
                profile.getSocialInsuranceBase(), DEFAULT_PENSION_INSURANCE_RATE);
        BigDecimal medicalInsurance = calculateInsuranceAmount(
                profile.getMedicalInsuranceBase(), profile.getMedicalInsuranceRate(),
                profile.getSocialInsuranceBase(), DEFAULT_MEDICAL_INSURANCE_RATE);
        BigDecimal unemploymentInsurance = calculateInsuranceAmount(
                profile.getUnemploymentInsuranceBase(), profile.getUnemploymentInsuranceRate(),
                profile.getSocialInsuranceBase(), DEFAULT_UNEMPLOYMENT_INSURANCE_RATE);
        BigDecimal socialInsurance = pensionInsurance.add(medicalInsurance).add(unemploymentInsurance).setScale(2, RoundingMode.HALF_UP);
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
        item.setPensionInsurance(pensionInsurance);
        item.setMedicalInsurance(medicalInsurance);
        item.setUnemploymentInsurance(unemploymentInsurance);
        item.setSocialInsurance(socialInsurance);
        item.setHousingFund(housingFund);
        item.setIncomeTax(incomeTax);
        item.setGrossSalary(gross);
        item.setDeductionTotal(deduction);
        item.setNetSalary(net);
        applyWarning(batch, employee.getId(), item, leaveDays);
        return item;
    }

    /**
     * 应用薪资警告。
     * @param batch 批次
     * @param employeeId 员工ID
     * @param item 薪资项
     * @param leaveDays 请假天数
     */
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
                item.setWarningReason("较上月实发工资波动超过30%");
                return;
            }
        }
        item.setWarningLevel(SalaryWarningLevelEnum.NONE.name());
        item.setWarningReason(null);
    }

    /**
     * 查找上月同员工的薪资。
     * @param employeeId 员工ID
     * @param salaryMonth 薪资月
     * @return 上月同员工的薪资
     */
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

    /**
     * 填充零薪资。
     * @param item 薪资项
     */
    private void fillZeroSalary(SalaryBatchItemEntity item) {
        item.setBaseSalary(ZERO);
        item.setAllowance(ZERO);
        item.setPerformanceBonus(ZERO);
        item.setOvertimePay(ZERO);
        item.setLateDeduction(ZERO);
        item.setLeaveDeduction(ZERO);
        item.setPensionInsurance(ZERO);
        item.setMedicalInsurance(ZERO);
        item.setUnemploymentInsurance(ZERO);
        item.setSocialInsurance(ZERO);
        item.setHousingFund(ZERO);
        item.setIncomeTax(ZERO);
        item.setGrossSalary(ZERO);
        item.setDeductionTotal(ZERO);
        item.setNetSalary(ZERO);
    }

    /**
     * 计算薪资所得税。
     * @param taxable 税前收入
     * @return 税后收入
     */
    private BigDecimal calculateIncomeTax(BigDecimal taxable) {
        BigDecimal threshold = new BigDecimal("5000");
        if (taxable.compareTo(threshold) <= 0) {
            return ZERO;
        }
        return taxable.subtract(threshold).multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 解析批次员工。
     * @param batch 批次
     * @return 员工列表
     */
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

    /**
     * 列出员工薪资档案。
     * @param employeeIds 员工ID列表
     * @return 员工薪资档案
     */
    private Map<Long, EmployeeSalaryProfileEntity> listProfiles(List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Map.of();
        }
        return employeeSalaryProfileMapper.selectList(Wrappers.lambdaQuery(EmployeeSalaryProfileEntity.class)
                        .in(EmployeeSalaryProfileEntity::getEmployeeId, employeeIds))

                //Stream 流是在数据库查询完成之后使用的，用来对查询结果进行进一步的处理和转换。
                .stream().collect(Collectors.toMap(EmployeeSalaryProfileEntity::getEmployeeId, Function.identity(), (a, b) -> a));
    }

    /**
     * 获取考勤汇总。
     * @param month 考勤月
     * @param employeeIds 员工ID列表
     * @return 考勤汇总
     */
    private Map<Long, AttendancePayrollSourceVO> getAttendanceSummary(String month, List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Map.of();
        }
        AttendanceService attendanceService = attendanceServiceProvider.getIfAvailable();
        if (attendanceService == null) {
            throw new GlobalException(ErrorCode.SYSTEM_ERROR, "考勤服务不可用，无法获取薪资核算所需考勤汇总");
        }
        List<AttendancePayrollSourceVO> summary = attendanceService.getPayrollSource(month, employeeIds);
        return summary.stream()

                //将一个包含 AttendancePayrollSourceVO 对象的列表转换为一个以员工 ID 为键的 Map
                      .collect(Collectors.toMap( //将列表转换为Map
                              AttendancePayrollSourceVO::getEmployeeId, //以员工ID为键
                              //VO对象为值
                              //Function.identity() :接收什么类型的参数就返回什么类型结果的函数。
                              //输入：一个 AttendancePayrollSourceVO 对象
                              //输出：返回同一个 AttendancePayrollSourceVO 对象（不做任何变换）
                              Function.identity(),

                              //如果有两个具有相同员工ID的对象，保留第一个（(a, b) -> a 表示保留左侧参数 a
                              (a, b) -> a
        ));
    }

    /**
     * 临时获取考勤月度汇总。
     * @param month 考勤月
     * @param employeeIds 员工ID列表
     * @return 考勤月度汇总
     */
    //private List<AttendancePayrollSourceVO> tempGetAttendanceMonthlySummary(String month, List<Long> employeeIds) {
    //    // 本方法未来替换为 hrms-business-attendance 的 AttendanceService#getPayrollSource(month, employeeIds)。
    //    return employeeIds.stream().map(employeeId -> AttendancePayrollSourceVO.builder()
    //            .employeeId(employeeId)
    //            .shouldAttendDays(22)
    //            .actualAttendDays(22)
    //            .lateCount(0)
    //            .earlyLeaveCount(0)
    //            .absenceDays(BigDecimal.ZERO)
    //            .leaveDays(BigDecimal.ZERO)
    //            .overtimeHours(BigDecimal.ZERO)
    //            .build()).toList();
    //}

    /**
     * 临时发布薪资核算消息。
     * @param message 消息
     */
    //private void tempPublishCalculateMessage(SalaryBatchCalculateMessage message) {
    //    // 本方法未来替换为 salary.batch.calculate Producer，用于异步触发薪资批次核算。
    //    log.info("temp salary.batch.calculate message: {}", JSONUtil.toJsonStr(message));
    //}

    /**
     * 临时启动薪资批次审批。
     * @param batch 批次
     * @return 审批ID
     */
    //private Long tempStartSalaryBatchApproval(SalaryBatchEntity batch) {
    //    // 本方法未来替换为 hrms-business-approval 的薪资批次审批发起接口。
    //    return Math.abs(IdUtil.getSnowflakeNextId());
    //}

    /**
     * 获取薪资批次。
     * @param id 薪资批次ID
     * @return 薪资批次
     */
    private SalaryBatchEntity getBatchRequired(Long id) {
        SalaryBatchEntity entity = salaryBatchMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "薪资批次不存在");
        }
        return entity;
    }

    /**
     * 列出员工。
     * @param employeeIds 员工ID列表
     * @return 员工列表
     */
    private Map<Long, SalaryEmployeeSnapshotEntity> listEmployeesByIds(List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Map.of();
        }
        return employeeSnapshotMapper.selectList(Wrappers.lambdaQuery(SalaryEmployeeSnapshotEntity.class)
                        .in(SalaryEmployeeSnapshotEntity::getId, employeeIds))
                        .stream().collect(
                                Collectors.toMap(SalaryEmployeeSnapshotEntity::getId,
                                        Function.identity(),
                                        (a, b) -> a));
    }

    /**
     * 薪资批次转VO。
     * @param entity 薪资批次
     * @return 薪资批次VO
     */
    private SalaryBatchVO toBatchVO(SalaryBatchEntity entity) {
        return SalaryBatchVO.builder()
                .id(entity.getId())
                .batchNo(entity.getBatchNo())
                .salaryMonth(entity.getSalaryMonth())
                .scopeType(entity.getScopeType())
                .scopeValue(entity.getScopeValue())
                .batchStatus(entity.getBatchStatus())
                .approvalInstanceId(entity.getApprovalInstanceId())
                .totalCount(entity.getTotalCount())
                .totalGrossSalary(entity.getTotalGrossSalary())
                .totalNetSalary(entity.getTotalNetSalary())
                .yellowWarningCount(entity.getYellowWarningCount())
                .redWarningCount(entity.getRedWarningCount())
                .blockCount(entity.getBlockCount())
                .build();
    }

    /**
     * 薪资批次项转VO。
     * @param item 薪资批次项
     * @param employee 员工
     * @return 薪资批次项VO
     */
    private SalaryBatchItemVO toBatchItemVO(SalaryBatchItemEntity item, SalaryEmployeeSnapshotEntity employee) {
        return batchItemBuilder(item, employee).build();
    }

    /**
     * 薪资批次项转VO。
     * @param item 薪资批次项
     * @param employee 员工
     * @return 薪资批次项VO
     */
    private SalaryBatchItemVO.SalaryBatchItemVOBuilder<?, ?> batchItemBuilder(SalaryBatchItemEntity item,
                                                                               SalaryEmployeeSnapshotEntity employee) {
        return SalaryBatchItemVO.builder()
                .id(item.getId())
                .batchId(item.getBatchId())
                .employeeId(item.getEmployeeId())
                .employeeNo(employee == null ? null : employee.getEmployeeNo())
                .employeeName(employee == null ? null : employee.getEmployeeName())
                .deptName(resolveDeptName(employee))
                .baseSalary(item.getBaseSalary())
                .allowance(item.getAllowance())
                .performanceBonus(item.getPerformanceBonus())
                .overtimePay(item.getOvertimePay())
                .lateDeduction(item.getLateDeduction())
                .leaveDeduction(item.getLeaveDeduction())
                .pensionInsurance(item.getPensionInsurance())
                .medicalInsurance(item.getMedicalInsurance())
                .unemploymentInsurance(item.getUnemploymentInsurance())
                .socialInsurance(item.getSocialInsurance())
                .housingFund(item.getHousingFund())
                .incomeTax(item.getIncomeTax())
                .grossSalary(item.getGrossSalary())
                .deductionTotal(item.getDeductionTotal())
                .netSalary(item.getNetSalary())
                .warningLevel(item.getWarningLevel())
                .warningReason(item.getWarningReason());
    }

    /**
     * 规范化薪资账套范围类型。
     * @param scopeType 范围类型
     * @return 规范化后的范围类型
     */
    private String normalizeScopeType(String scopeType) {
        if (StrUtil.isBlank(scopeType)) {
            return "ALL";
        }
        String normalized = scopeType.trim().toUpperCase();
        if ("DEPARTMENT".equals(normalized)) {
            return "DEPT";
        }
        if ("LEVEL".equals(normalized)) {
            return "JOB_LEVEL";
        }
        if (!Set.of("ALL", "DEPT", "EMPLOYEE", "JOB_LEVEL").contains(normalized)) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "范围类型仅支持 ALL/DEPT/EMPLOYEE/JOB_LEVEL");
        }
        return normalized;
    }

    /**
     * 规范化薪资批次范围值。
     *
     * @param scopeType  范围类型
     * @param requestDTO 请求参数
     * @return 规范化后的范围值
     * 本方法使用的工具类: CollUtil(hutool),StrUtil(hutool),Collectors(JDK)
     */
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

    /**
     * 规范化薪资批次查询范围值。
     *
     * @param scopeType  范围类型
     * @param scopeValue 范围值
     * @return 规范化后的范围值
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private String normalizeBatchScopeValue(String scopeType, String scopeValue) {
        if ("ALL".equals(scopeType)) {
            return null;
        }
        if (StrUtil.isBlank(scopeValue)) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "非 ALL 范围必须传入 scopeValue");
        }
        return scopeValue.trim();
    }

    /**
     * 校验当前用户是否具备薪资管理操作角色。
     *
     * @return 无返回值
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),RoleService(hrms-system-auth),StrUtil(hutool)
     */
    private void assertSalaryManagerRole() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "无薪资管理权限");
        }
        boolean allowed = roleService.getRolesByUserId(userId).stream()
                .map(RoleEntity::getRoleCode)
                .filter(StrUtil::isNotBlank)
                .map(roleCode -> roleCode.trim().toUpperCase())
                .anyMatch(SALARY_MANAGER_ROLE_CODES::contains);
        if (!allowed) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "无薪资管理权限");
        }
    }

    private int normalizeTrendMonths(Integer months) {
        int value = Optional.ofNullable(months).orElse(6);
        if (value < 1 || value > 24) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "趋势月份数量必须在 1 到 24 之间");
        }
        return value;
    }

    /**
     * 构建单月薪资趋势。
     *
     * @param month   薪资月份
     * @param batches 当月薪资批次
     * @return 单月薪资趋势
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private SalaryBatchTrendVO buildBatchTrend(String month, List<SalaryBatchEntity> batches) {
        BigDecimal grossSalary = ZERO;
        BigDecimal netSalary = ZERO;
        int employeeCount = 0;
        for (SalaryBatchEntity batch : batches) {
            grossSalary = grossSalary.add(money(batch.getTotalGrossSalary()));
            netSalary = netSalary.add(money(batch.getTotalNetSalary()));
            employeeCount += Optional.ofNullable(batch.getTotalCount()).orElse(0);
        }
        return SalaryBatchTrendVO.builder()
                .month(month)
                .grossSalary(money(grossSalary))
                .netSalary(money(netSalary))
                .employeeCount(employeeCount)
                .build();
    }

    /**
     * 规范化薪资月份。
     *
     * @param month 薪资月份
     * @return 规范化后的薪资月份
     * 本方法使用的工具类: StrUtil(hutool),YearMonth(JDK)
     */
    private String normalizeMonth(String month) {
        if (StrUtil.isBlank(month)) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "薪资月份不能为空");
        }
        return YearMonth.parse(month).toString();
    }

    /**
     * 解析 Long 列表。
     * @param value 值
     * @return Long 列表
     */
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

    /**
     * 规范化人工调整薪资项目编码。
     *
     * @param itemCode 薪资项目编码
     * @return 规范化后的薪资项目编码
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private String normalizeAdjustmentItemCode(String itemCode) {
        if (StrUtil.isBlank(itemCode)) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "薪资项目编码不能为空");
        }
        String normalized = itemCode.trim().toUpperCase();
        if (!SALARY_ADJUST_ITEM_CODES.contains(normalized)) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "不支持的薪资调整项目");
        }
        return normalized;
    }

    /**
     * 将人工调整金额应用到薪资明细。
     *
     * @param item         薪资明细
     * @param itemCode     薪资项目编码
     * @param adjustAmount 调整金额
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private void applyAdjustmentToItem(SalaryBatchItemEntity item, String itemCode, BigDecimal adjustAmount) {
        BigDecimal amount = money(adjustAmount);
        switch (itemCode) {
            case "BASE_SALARY" -> item.setBaseSalary(money(item.getBaseSalary()).add(amount));
            case "ALLOWANCE" -> item.setAllowance(money(item.getAllowance()).add(amount));
            case "PERFORMANCE_BONUS" -> item.setPerformanceBonus(money(item.getPerformanceBonus()).add(amount));
            case "OVERTIME_PAY" -> item.setOvertimePay(money(item.getOvertimePay()).add(amount));
            case "LATE_DEDUCTION" -> item.setLateDeduction(money(item.getLateDeduction()).add(amount));
            case "LEAVE_DEDUCTION" -> item.setLeaveDeduction(money(item.getLeaveDeduction()).add(amount));
            case "SOCIAL_INSURANCE" -> item.setSocialInsurance(money(item.getSocialInsurance()).add(amount));
            case "PENSION_INSURANCE" -> {
                item.setPensionInsurance(money(item.getPensionInsurance()).add(amount));
                syncSocialInsuranceTotal(item);
            }
            case "MEDICAL_INSURANCE" -> {
                item.setMedicalInsurance(money(item.getMedicalInsurance()).add(amount));
                syncSocialInsuranceTotal(item);
            }
            case "UNEMPLOYMENT_INSURANCE" -> {
                item.setUnemploymentInsurance(money(item.getUnemploymentInsurance()).add(amount));
                syncSocialInsuranceTotal(item);
            }
            case "HOUSING_FUND" -> item.setHousingFund(money(item.getHousingFund()).add(amount));
            case "INCOME_TAX" -> item.setIncomeTax(money(item.getIncomeTax()).add(amount));
            default -> throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "不支持的薪资调整项目");
        }
    }

    /**
     * 重新计算员工薪资明细汇总金额。
     *
     * @param item 薪资明细
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private void recalculateItemAmount(SalaryBatchItemEntity item) {
        BigDecimal gross = money(item.getBaseSalary())
                .add(money(item.getAllowance()))
                .add(money(item.getPerformanceBonus()))
                .add(money(item.getOvertimePay()));
        BigDecimal deduction = money(item.getLateDeduction())
                .add(money(item.getLeaveDeduction()))
                .add(money(item.getSocialInsurance()))
                .add(money(item.getHousingFund()))
                .add(money(item.getIncomeTax()));
        item.setGrossSalary(money(gross));
        item.setDeductionTotal(money(deduction));
        item.setNetSalary(money(gross.subtract(deduction)));
        if (item.getNetSalary().compareTo(BigDecimal.ZERO) <= 0) {
            item.setWarningLevel(SalaryWarningLevelEnum.BLOCK.name());
            item.setWarningReason("实发工资小于等于 0");
        } else if (SalaryWarningLevelEnum.BLOCK.name().equals(item.getWarningLevel())
                && "实发工资小于等于 0".equals(item.getWarningReason())) {
            item.setWarningLevel(SalaryWarningLevelEnum.NONE.name());
            item.setWarningReason(null);
        }
    }

    /**
     * 刷新薪资批次汇总数据。
     *
     * @param batchId 薪资批次ID
     * 本方法使用的工具类: Wrappers(MyBatis-Plus),BigDecimal(JDK)
     */
    private void refreshBatchSummary(Long batchId) {
        SalaryBatchEntity batch = getBatchRequired(batchId);
        List<SalaryBatchItemEntity> items = salaryBatchItemMapper.selectList(Wrappers.lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getBatchId, batchId));
        BigDecimal totalGross = ZERO;
        BigDecimal totalNet = ZERO;
        int yellow = 0;
        int red = 0;
        int block = 0;
        for (SalaryBatchItemEntity item : items) {
            totalGross = totalGross.add(money(item.getGrossSalary()));
            totalNet = totalNet.add(money(item.getNetSalary()));
            if (SalaryWarningLevelEnum.YELLOW.name().equals(item.getWarningLevel())) {
                yellow++;
            } else if (SalaryWarningLevelEnum.RED.name().equals(item.getWarningLevel())) {
                red++;
            } else if (SalaryWarningLevelEnum.BLOCK.name().equals(item.getWarningLevel())) {
                block++;
            }
        }
        batch.setTotalCount(items.size());
        batch.setTotalGrossSalary(money(totalGross));
        batch.setTotalNetSalary(money(totalNet));
        batch.setYellowWarningCount(yellow);
        batch.setRedWarningCount(red);
        batch.setBlockCount(block);
        salaryBatchMapper.updateById(batch);
    }

    /**
     * 应用薪资批次全部有效人工调整。
     *
     * @param batchId 薪资批次ID
     * 本方法使用的工具类: Wrappers(MyBatis-Plus),Collectors(JDK)
     */
    private void applyBatchAdjustments(Long batchId) {
        List<SalaryBatchAdjustmentEntity> adjustments = salaryBatchAdjustmentMapper.selectList(
                Wrappers.lambdaQuery(SalaryBatchAdjustmentEntity.class)
                        .eq(SalaryBatchAdjustmentEntity::getBatchId, batchId));
        if (CollUtil.isEmpty(adjustments)) {
            refreshBatchSummary(batchId);
            return;
        }
        Map<Long, List<SalaryBatchAdjustmentEntity>> adjustmentMap = adjustments.stream()
                .collect(Collectors.groupingBy(SalaryBatchAdjustmentEntity::getEmployeeId));
        List<SalaryBatchItemEntity> items = salaryBatchItemMapper.selectList(Wrappers.lambdaQuery(SalaryBatchItemEntity.class)
                .eq(SalaryBatchItemEntity::getBatchId, batchId));
        for (SalaryBatchItemEntity item : items) {
            List<SalaryBatchAdjustmentEntity> employeeAdjustments = adjustmentMap.get(item.getEmployeeId());
            if (CollUtil.isEmpty(employeeAdjustments)) {
                continue;
            }
            for (SalaryBatchAdjustmentEntity adjustment : employeeAdjustments) {
                applyAdjustmentToItem(item, adjustment.getItemCode(), adjustment.getAdjustAmount());
            }
            recalculateItemAmount(item);
            salaryBatchItemMapper.updateById(item);
        }
        refreshBatchSummary(batchId);
    }

    /**
     * 格式化金额。
     *
     * @param value 值
     * @return 格式化后的金额
     * 本方法使用的工具类: BigDecimal(JDK),Optional(JDK)
     */
    private BigDecimal money(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算险种金额。
     *
     * @param insuranceBase 险种基数
     * @param insuranceRate 险种比例
     * @param legacyBase 旧社保基数
     * @param defaultRate 默认比例
     * @return 险种金额
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private BigDecimal calculateInsuranceAmount(BigDecimal insuranceBase,
                                                BigDecimal insuranceRate,
                                                BigDecimal legacyBase,
                                                BigDecimal defaultRate) {
        BigDecimal base = resolveInsuranceBase(insuranceBase, legacyBase, null);
        BigDecimal ratio = resolveInsuranceRate(insuranceRate, null, defaultRate);
        return money(base.multiply(ratio));
    }

    /**
     * 同步社保合计金额。
     *
     * @param item 薪资明细
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private void syncSocialInsuranceTotal(SalaryBatchItemEntity item) {
        item.setSocialInsurance(money(item.getPensionInsurance())
                .add(money(item.getMedicalInsurance()))
                .add(money(item.getUnemploymentInsurance())));
    }

    private BigDecimal rate(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 解析险种基数。
     *
     * @param requestBase 请求值
     * @param legacyBase  旧社保基数
     * @param existingBase 已有值
     * @return 险种基数
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private BigDecimal resolveInsuranceBase(BigDecimal requestBase, BigDecimal legacyBase, BigDecimal existingBase) {
        if (requestBase != null) {
            return money(requestBase);
        }
        if (legacyBase != null) {
            return money(legacyBase);
        }
        if (existingBase != null) {
            return money(existingBase);
        }
        return ZERO;
    }

    /**
     * 解析险种比例。
     *
     * @param requestRate 请求值
     * @param existingRate 已有值
     * @param defaultRate 默认值
     * @return 险种比例
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private BigDecimal resolveInsuranceRate(BigDecimal requestRate, BigDecimal existingRate, BigDecimal defaultRate) {
        if (requestRate != null) {
            return rate(requestRate);
        }
        if (existingRate != null) {
            return rate(existingRate);
        }
        return rate(defaultRate);
    }

    /**
     * 解析兼容社保合计基数。
     *
     * @param legacyBase 旧社保基数
     * @param pensionBase 养老保险基数
     * @param medicalBase 医疗保险基数
     * @param unemploymentBase 失业保险基数
     * @return 兼容社保合计基数
     * 本方法使用的工具类: Objects(JDK)
     */
    private BigDecimal resolveCompatibleSocialInsuranceBase(BigDecimal legacyBase,
                                                            BigDecimal pensionBase,
                                                            BigDecimal medicalBase,
                                                            BigDecimal unemploymentBase) {
        if (legacyBase != null) {
            return money(legacyBase);
        }
        BigDecimal pension = money(pensionBase);
        BigDecimal medical = money(medicalBase);
        BigDecimal unemployment = money(unemploymentBase);
        if (Objects.equals(pension, medical) && Objects.equals(pension, unemployment)) {
            return pension;
        }
        return pension;
    }

    private String buildSalaryExportFileName(SalaryBatchEntity batch) {
        return "薪资核算_" + batch.getSalaryMonth() + "_" + sanitizeFileName(batch.getBatchNo()) + ".xlsx";
    }

    private Path buildSalaryExportPath(SalaryBatchEntity batch) {
        try {
            Path exportDir = Path.of(fileConfig.getBaseDir(), "salary-batch");
            Files.createDirectories(exportDir);
            String storageName = "salary-batch-" + batch.getId() + "-" + System.currentTimeMillis() + ".xlsx";
            return exportDir.resolve(storageName);
        } catch (IOException ex) {
            throw new GlobalException(ErrorCode.FILE_UPLOAD_ERROR, "创建导出目录失败");
        }
    }

    private long resolveFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ex) {
            throw new GlobalException(ErrorCode.FILE_UPLOAD_ERROR, "读取导出文件大小失败");
        }
    }

    private void writeSalaryBatchWorkbook(Path filePath, List<SalaryBatchItemVO> items) {
        try (Workbook workbook = new XSSFWorkbook();
             OutputStream outputStream = Files.newOutputStream(filePath)) {
            Sheet sheet = workbook.createSheet("薪资核算");
            writeSalaryBatchHeader(sheet.createRow(0));
            for (int index = 0; index < items.size(); index++) {
                writeSalaryBatchRow(sheet.createRow(index + 1), items.get(index));
            }
            for (int index = 0; index < 12; index++) {
                sheet.setColumnWidth(index, 15 * 256);
            }
            workbook.write(outputStream);
        } catch (IOException ex) {
            throw new GlobalException(ErrorCode.FILE_UPLOAD_ERROR, "写出薪资导出文件失败");
        }
    }

    private void writeSalaryBatchHeader(Row row) {
        String[] headers = {
                "工号", "姓名", "部门", "基本工资", "补贴", "绩效", "加班",
                "应发合计", "社保", "公积金", "个税", "实发工资"
        };
        for (int index = 0; index < headers.length; index++) {
            row.createCell(index).setCellValue(headers[index]);
        }
    }

    private void writeSalaryBatchRow(Row row, SalaryBatchItemVO item) {
        row.createCell(0).setCellValue(StrUtil.blankToDefault(item.getEmployeeNo(), ""));
        row.createCell(1).setCellValue(StrUtil.blankToDefault(item.getEmployeeName(), ""));
        row.createCell(2).setCellValue(StrUtil.blankToDefault(item.getDeptName(), ""));
        row.createCell(3).setCellValue(toExcelNumber(item.getBaseSalary()));
        row.createCell(4).setCellValue(toExcelNumber(item.getAllowance()));
        row.createCell(5).setCellValue(toExcelNumber(item.getPerformanceBonus()));
        row.createCell(6).setCellValue(toExcelNumber(item.getOvertimePay()));
        row.createCell(7).setCellValue(toExcelNumber(item.getGrossSalary()));
        row.createCell(8).setCellValue(toExcelNumber(item.getSocialInsurance()));
        row.createCell(9).setCellValue(toExcelNumber(item.getHousingFund()));
        row.createCell(10).setCellValue(toExcelNumber(item.getIncomeTax()));
        row.createCell(11).setCellValue(toExcelNumber(item.getNetSalary()));
    }

    private double toExcelNumber(BigDecimal value) {
        return money(value).doubleValue();
    }

    private String sanitizeFileName(String value) {
        if (StrUtil.isBlank(value)) {
            return "BATCH";
        }
        return value.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    /**
     * 解析员工部门名称。
     *
     * @param employee 员工快照
     * @return 部门名称
     * 本方法使用的工具类: DeptService(hrms-system-organization)
     */
    private String resolveDeptName(SalaryEmployeeSnapshotEntity employee) {
        if (employee == null || employee.getDeptId() == null) {
            return null;
        }
        try {
            DeptDetailVO dept = deptService.getDeptById(employee.getDeptId());
            return dept == null ? null : dept.getDeptName();
        } catch (GlobalException ex) {
            if (ex.getErrorCode() != null && ex.getErrorCode().getCode() == ErrorCode.NOT_FOUND.getCode()) {
                log.warn("Salary preview dept missing, employeeId={}, deptId={}", employee.getId(), employee.getDeptId());
                return "未知部门";
            }
            throw ex;
        }
    }

    /**
     * 删除薪资批次缓存。
     * @param batchId 薪资批次ID
     */
    private void evictBatchCache(Long batchId) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.delete(SalaryCacheKeys.batchPreview(batchId));
        }
    }
}
