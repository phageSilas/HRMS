package com.hrms.business.salary.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.salary.cache.SalaryCacheKeys;
import com.hrms.business.salary.dto.SalaryManagePayslipQueryDTO;
import com.hrms.business.salary.dto.SalaryManagePayslipVerifyRequestDTO;
import com.hrms.business.salary.dto.SalaryPayslipPageQueryDTO;
import com.hrms.business.salary.dto.SalaryPayslipVerifyRequestDTO;
import com.hrms.business.salary.entity.SalaryBatchEntity;
import com.hrms.business.salary.entity.SalaryBatchItemEntity;
import com.hrms.business.salary.entity.SalaryEmployeeSnapshotEntity;
import com.hrms.business.salary.entity.SalaryPayslipViewRecordEntity;
import com.hrms.business.salary.entity.SalarySysUserEntity;
import com.hrms.business.salary.common.enums.SalaryBatchStatusEnum;
import com.hrms.business.salary.service.PaySlipService;
import com.hrms.business.salary.vo.SalaryManagePayslipPageVO;
import com.hrms.business.salary.vo.SalaryPayslipDetailVO;
import com.hrms.business.salary.vo.SalaryPayslipListVO;
import com.hrms.business.salary.vo.SalaryPayslipVerifyVO;
import com.hrms.business.salary.vo.SalaryTrendVO;
import com.hrms.business.salary.mapper.SalaryBatchItemMapper;
import com.hrms.business.salary.mapper.SalaryBatchMapper;
import com.hrms.business.salary.mapper.SalaryEmployeeSnapshotMapper;
import com.hrms.business.salary.mapper.SalaryPayslipViewRecordMapper;
import com.hrms.business.salary.mapper.SalarySysUserMapper;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.vo.DeptDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hrms.business.salary.common.constant.PaySlipConstant.*;

/**
 * 工资条服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaySlipServiceImpl implements PaySlipService {


    private final SalaryBatchMapper salaryBatchMapper;
    private final SalaryBatchItemMapper salaryBatchItemMapper;
    private final SalaryPayslipViewRecordMapper salaryPayslipViewRecordMapper;
    private final SalaryEmployeeSnapshotMapper employeeSnapshotMapper;
    private final SalarySysUserMapper salarySysUserMapper;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;
    private final RoleService roleService;
    private final DeptService deptService;

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
    /**
     * 分页查询当前员工工资条列表。
     *
     * @param queryDTO 查询参数
     * @return 工资条分页结果
     * 本方法使用的工具类: Page(MyBatis-Plus),PageResult(hrms-common),StringRedisTemplate(spring-data-redis)
     */
    @Override
    public PageResult<SalaryPayslipListVO> pagePayslips(SalaryPayslipPageQueryDTO queryDTO) {
        Long employeeId = getCurrentEmployeeId();
        // Optional.ofNullable:如果传入的值不为null，ofNullable 会创建一个包含该值的 Optional 对象；如果传入的值为null，则创建一个空的 Optional 对象。
        // 获取查询参数的页码和页大小，默认值分别为1和10
        int pageNum = Optional.ofNullable(queryDTO.getPageNum()).orElse(1);
        int pageSize = Optional.ofNullable(queryDTO.getPageSize()).orElse(10);
        // 分页查询当前员工的工资条列表
        Page<SalaryPayslipListVO> page = salaryBatchItemMapper.selectEmployeePayslipPage(
                Page.of(pageNum, pageSize), employeeId, queryDTO, PAYSLIP_VISIBLE_STATUS);
        // 判断Redis模板是否可用，如果可用则设置验证状态
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        // 遍历分页结果，设置验证状态，如果Redis缓存可用则从Redis中获取验证状态，不再进行二次验证，否则设置为false
        if (redisTemplate != null) {
            page.getRecords().forEach(record -> record.setVerified(redisTemplate.hasKey(
                    SalaryCacheKeys.payslipVerify(employeeId, record.getSalaryMonth()))));
        } else {
            page.getRecords().forEach(record -> record.setVerified(false));
        }
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public SalaryPayslipVerifyVO verifyPayslip(SalaryPayslipVerifyRequestDTO requestDTO) {
        // 获取当前用户ID和员工ID，判断用户是否可用
        Long userId = SecurityContextHolder.getUserId();
        Long employeeId = getCurrentEmployeeId();
        SalarySysUserEntity user = salarySysUserMapper.selectById(userId);
        if (user == null || Objects.equals(user.getIsDeleted(), 1) || Objects.equals(user.getStatus(), 0)) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED, "当前用户不可用");
        }
        // 验证登录密码
        boolean passwordOk = StrUtil.isNotBlank(requestDTO.getPassword())
                && getPasswordEncoder().matches(requestDTO.getPassword(), user.getPassword());
        if (!passwordOk) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "登录密码验证失败");
        }
        // 生成验证令牌
        String token = IdUtil.fastSimpleUUID();
        //ObjectProvider：适用于Redis是可选依赖的场景，如：缓存功能（没有Redis就直接访问数据库）
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        // 判断Redis缓存是否可用，如果可用则保存验证令牌
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(SalaryCacheKeys.payslipVerify(employeeId, requestDTO.getMonth()),
                    token, 30, TimeUnit.MINUTES);
        }
        return SalaryPayslipVerifyVO.builder()
                .success(true)
                .token(token)
                .expireTime(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    /**
     * 管理端工资条二次验证。
     *
     * @param requestDTO 验证请求
     * @return 验证结果
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),PasswordEncoder(spring-security-crypto),StringRedisTemplate(spring-data-redis),IdUtil(hutool)
     */
    @Override
    public SalaryPayslipVerifyVO verifyManagePayslip(SalaryManagePayslipVerifyRequestDTO requestDTO) {
        // 判断当前用户是否具有工资条管理权限
        assertSalaryManagerRole();
        Long userId = SecurityContextHolder.getUserId();
        SalarySysUserEntity user = salarySysUserMapper.selectById(userId);
        if (user == null || Objects.equals(user.getIsDeleted(), 1) || Objects.equals(user.getStatus(), 0)) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED, "当前用户不可用");
        }
        // 验证登录密码
        boolean passwordOk = StrUtil.isNotBlank(requestDTO.getPassword())
                && getPasswordEncoder().matches(requestDTO.getPassword(), user.getPassword());
        if (!passwordOk) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "登录密码验证失败");
        }
        // 生成验证令牌并保存到Redis缓存中，设置有效期为30分钟
        String token = IdUtil.fastSimpleUUID();
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(SalaryCacheKeys.managePayslipVerify(userId), token, 30, java.util.concurrent.TimeUnit.MINUTES);
        }
        return SalaryPayslipVerifyVO.builder()
                .success(true)
                .token(token)
                .expireTime(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    /**
     * 分页查询管理端工资条列表。
     *
     * @param queryDTO 查询参数
     * @return 工资条分页结果
     * 本方法使用的工具类: Page(MyBatis-Plus),PageResult(hrms-common),SecurityContextHolder(hrms-common)
     */
    @Override
    public PageResult<SalaryManagePayslipPageVO> pageManagePayslips(SalaryManagePayslipQueryDTO queryDTO) {
        assertSalaryManagerRole();
        queryDTO.setViewStatus(normalizeManagePayslipViewStatus(queryDTO.getViewStatus()));
        int pageNum = Optional.ofNullable(queryDTO.getPageNum()).orElse(1);
        int pageSize = Optional.ofNullable(queryDTO.getPageSize()).orElse(10);
        Page<SalaryManagePayslipPageVO> page = salaryBatchItemMapper.selectManagePayslipPage(
                Page.of(pageNum, pageSize), queryDTO, PAYSLIP_VISIBLE_STATUS);
        boolean verified = hasManagePayslipVerified();
        page.getRecords().forEach(record -> {
            SalaryEmployeeSnapshotEntity employee = new SalaryEmployeeSnapshotEntity();
            employee.setId(record.getEmployeeId());
            employee.setDeptId(record.getDeptId());
            record.setDeptName(resolveDeptName(employee));
            record.setVerified(verified);
        });
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
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
        // 获取工资批次
        SalaryBatchEntity batch = getBatchRequired(item.getBatchId());
        if (!PAYSLIP_VISIBLE_STATUS.contains(batch.getBatchStatus())) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "工资条暂不可查看");
        }
        // 判断Redis缓存是否可用，如果可用则判断验证令牌是否存在
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null && !redisTemplate.hasKey(
                SalaryCacheKeys.payslipVerify(employeeId, batch.getSalaryMonth()))) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "请先完成工资条二次验证");
        }
        SalaryEmployeeSnapshotEntity employee = employeeSnapshotMapper.selectById(employeeId);
        recordPayslipView(item, batch);
        return payslipDetailBuilder(item, employee)
                .salaryMonth(batch.getSalaryMonth())
                .batchNo(batch.getBatchNo())
                .build();
    }

    /**
     * 查询当前员工近 6 个月薪资趋势。
     *
     * @return 薪资趋势
     * 本方法使用的工具类: YearMonth(JDK),Wrappers(MyBatis-Plus),List(JDK)
     */
    /**
     * 查询管理端工资条详情。
     *
     * @param payslipId 工资条ID
     * @return 工资条详情
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),StringRedisTemplate(spring-data-redis),Wrappers(MyBatis-Plus)
     */
    @Override
    public SalaryPayslipDetailVO getManagePayslipDetail(Long payslipId) {
        // 判断当前用户是否具有工资条管理权限
        assertSalaryManagerRole();
        if (!hasManagePayslipVerified()) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "请先完成管理端工资条二次验证");
        }
        SalaryBatchItemEntity item = salaryBatchItemMapper.selectById(payslipId);
        if (item == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "工资条不存在");
        }
        SalaryBatchEntity batch = getBatchRequired(item.getBatchId());
        SalaryEmployeeSnapshotEntity employee = employeeSnapshotMapper.selectById(item.getEmployeeId());
        return payslipDetailBuilder(item, employee)
                .salaryMonth(batch.getSalaryMonth())
                .batchNo(batch.getBatchNo())
                .build();
    }

    /**
     * 查询当前员工近 6 个月薪资趋势。
     * @return 薪资趋势
     */
    @Override
    public List<SalaryTrendVO> getTrend() {
        Long employeeId = getCurrentEmployeeId();
        String startMonth = YearMonth.now().minusMonths(5).toString();
        List<SalaryBatchEntity> batches = salaryBatchMapper.selectList(Wrappers.lambdaQuery(SalaryBatchEntity.class)
                .ge(SalaryBatchEntity::getSalaryMonth, startMonth)// 大于等于
                .in(SalaryBatchEntity::getBatchStatus, PAYSLIP_VISIBLE_STATUS)// 在...内
                .orderByAsc(SalaryBatchEntity::getSalaryMonth));// 按工资月份升序排序
        // 判断是否存在可查看的薪资批次,若不存在则返回空列表
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
                .map(item -> SalaryTrendVO.builder()
                        .month(batchMap.get(item.getBatchId()).getSalaryMonth())
                        .netSalary(item.getNetSalary())
                        .build())
                .sorted(Comparator.comparing(SalaryTrendVO::getMonth))
                .toList();
    }

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
     * 获取当前登录用户绑定的员工ID。
     * @return 员工ID
     */
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

    /**
     * 薪资明细转VO。
     * @param item 薪资明细
     * @param employee 员工
     * @return 薪资明细VO
     */
    private SalaryPayslipDetailVO.SalaryPayslipDetailVOBuilder<?, ?> payslipDetailBuilder(SalaryBatchItemEntity item,
                                                                                           SalaryEmployeeSnapshotEntity employee) {
        return SalaryPayslipDetailVO.builder()
                .id(item.getId())
                .batchId(item.getBatchId())
                .employeeId(item.getEmployeeId())
                .employeeNo(employee == null ? null : employee.getEmployeeNo())
                .employeeName(employee == null ? null : employee.getEmployeeName())
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
     * 薪资 payroll 列表转VO。
     * @param item 薪资 payroll
     * @param batch 薪资批次
     * @return 薪资 payrollVO
     */
    private SalaryPayslipListVO toPayslipListVO(SalaryBatchItemEntity item, SalaryBatchEntity batch) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        return SalaryPayslipListVO.builder()
                .id(item.getId())
                .salaryMonth(batch.getSalaryMonth())
                .grossSalary(item.getGrossSalary())
                .deductionTotal(item.getDeductionTotal())
                .netSalary(item.getNetSalary())
                .batchStatus(batch.getBatchStatus())
                .verified(redisTemplate != null && redisTemplate.hasKey(
                        SalaryCacheKeys.payslipVerify(item.getEmployeeId(), batch.getSalaryMonth())))
                .build();
    }

    /**
     * 校验当前用户是否具备薪资管理操作角色。
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),RoleService(hrms-system-auth),StrUtil(hutool)
     */
    private void assertSalaryManagerRole() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "无薪资管理权限");
        }
        boolean allowed = roleService.getRolesByUserId(userId).stream()
                .map(RoleEntity::getRoleCode)// 获取角色代码
                .filter(StrUtil::isNotBlank)// 过滤掉空字符串
                .map(roleCode -> roleCode.trim().toUpperCase())// 去除空格并转换为大写
                .anyMatch(SALARY_MANAGER_ROLE_CODES::contains);// 匹配任意一个角色代码
        if (!allowed) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "无薪资管理权限");
        }
    }

    /**
     * 规范化管理端工资条查看状态。
     *
     * @param viewStatus 查看状态
     * @return 规范化后的查看状态
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private String normalizeManagePayslipViewStatus(String viewStatus) {
        if (StrUtil.isBlank(viewStatus)) {
            return null;
        }
        String normalized = viewStatus.trim().toUpperCase();
        if (!PAYSLIP_MANAGE_VIEW_STATUS.contains(normalized)) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "查看状态仅支持 VIEWED/UNVIEWED/UNPUBLISHED");
        }
        return normalized;
    }

    /**
     * 判断管理端工资条是否已完成二次验证。
     *
     * @return 是否已验证
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),StringRedisTemplate(spring-data-redis)
     */
    private boolean hasManagePayslipVerified() {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        return redisTemplate != null && redisTemplate.hasKey(
                SalaryCacheKeys.managePayslipVerify(SecurityContextHolder.getUserId()));
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
     * 记录员工工资条查看行为。
     *
     * @param item  工资条明细
     * @param batch 薪资批次
     * 本方法使用的工具类: Wrappers(MyBatis-Plus),LocalDateTime(JDK)
     */
    private void recordPayslipView(SalaryBatchItemEntity item, SalaryBatchEntity batch) {
        SalaryPayslipViewRecordEntity record = salaryPayslipViewRecordMapper.selectOne(Wrappers
                .lambdaQuery(SalaryPayslipViewRecordEntity.class)
                .eq(SalaryPayslipViewRecordEntity::getPayslipItemId, item.getId())
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new SalaryPayslipViewRecordEntity();
            record.setPayslipItemId(item.getId());
            record.setBatchId(item.getBatchId());
            record.setEmployeeId(item.getEmployeeId());
            record.setSalaryMonth(batch.getSalaryMonth());
            record.setFirstViewTime(now);
            record.setLastViewTime(now);
            record.setViewCount(1);
            salaryPayslipViewRecordMapper.insert(record);
            return;
        }
        record.setLastViewTime(now);
        record.setViewCount(Optional.ofNullable(record.getViewCount()).orElse(0) + 1);
        salaryPayslipViewRecordMapper.updateById(record);
    }

    /**
     * 获取密码编码器。
     *
     * @return 密码编码器
     * 本方法使用的工具类: Optional(java.util),BCryptPasswordEncoder(spring-security-crypto)
     */
    private PasswordEncoder getPasswordEncoder() {
        return Optional.ofNullable(passwordEncoderProvider.getIfAvailable()).orElseGet(BCryptPasswordEncoder::new);
    }
}
