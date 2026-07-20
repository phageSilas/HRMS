package com.hrms.business.salary.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.salary.cache.SalaryCacheKeys;
import com.hrms.business.salary.dto.EmployeeSalaryProfileRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateCreateOrUpdateRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateItemRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateQueryDTO;
import com.hrms.business.salary.entity.EmployeeSalaryProfileEntity;
import com.hrms.business.salary.entity.SalaryEmployeeSnapshotEntity;
import com.hrms.business.salary.entity.SalaryTemplateEntity;
import com.hrms.business.salary.entity.SalaryTemplateItemEntity;
import com.hrms.business.salary.mapper.EmployeeSalaryProfileMapper;
import com.hrms.business.salary.mapper.SalaryEmployeeSnapshotMapper;
import com.hrms.business.salary.mapper.SalaryTemplateItemMapper;
import com.hrms.business.salary.mapper.SalaryTemplateMapper;
import com.hrms.business.salary.service.SalaryTemplateService;
import com.hrms.business.salary.vo.EmployeeSalaryProfileVO;
import com.hrms.business.salary.vo.SalaryTemplateItemVO;
import com.hrms.business.salary.vo.SalaryTemplatePageVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 薪资账套服务实现。
 */
@Service
@RequiredArgsConstructor
public class SalaryTemplateServiceImpl implements SalaryTemplateService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO_RATE = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    private static final BigDecimal DEFAULT_PENSION_INSURANCE_RATE = new BigDecimal("0.0800");
    private static final BigDecimal DEFAULT_MEDICAL_INSURANCE_RATE = new BigDecimal("0.0200");
    private static final BigDecimal DEFAULT_UNEMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.0050");

    private final SalaryTemplateMapper salaryTemplateMapper;
    private final SalaryTemplateItemMapper salaryTemplateItemMapper;
    private final EmployeeSalaryProfileMapper employeeSalaryProfileMapper;
    private final SalaryEmployeeSnapshotMapper employeeSnapshotMapper;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

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
                .eq(queryDTO.getStatus() != null, SalaryTemplateEntity::getStatus, queryDTO.getStatus());

        // 添加账套范围条件
        appendTemplateScopeCondition(wrapper, queryDTO.getScope());
        wrapper.orderByDesc(SalaryTemplateEntity::getCreateTime);
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
        entity.setEffectiveDate(requestDTO.getEffectiveDate());
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
        entity.setEffectiveDate(requestDTO.getEffectiveDate());
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
        BigDecimal pensionInsuranceBase = resolveInsuranceBase(
                requestDTO.getPensionInsuranceBase(), requestDTO.getSocialInsuranceBase(), profile.getPensionInsuranceBase());
        BigDecimal medicalInsuranceBase = resolveInsuranceBase(
                requestDTO.getMedicalInsuranceBase(), requestDTO.getSocialInsuranceBase(), profile.getMedicalInsuranceBase());
        BigDecimal unemploymentInsuranceBase = resolveInsuranceBase(
                requestDTO.getUnemploymentInsuranceBase(), requestDTO.getSocialInsuranceBase(), profile.getUnemploymentInsuranceBase());
        profile.setPensionInsuranceBase(pensionInsuranceBase);
        profile.setPensionInsuranceRate(resolveInsuranceRate(
                requestDTO.getPensionInsuranceRate(), profile.getPensionInsuranceRate(), DEFAULT_PENSION_INSURANCE_RATE));
        profile.setMedicalInsuranceBase(medicalInsuranceBase);
        profile.setMedicalInsuranceRate(resolveInsuranceRate(
                requestDTO.getMedicalInsuranceRate(), profile.getMedicalInsuranceRate(), DEFAULT_MEDICAL_INSURANCE_RATE));
        profile.setUnemploymentInsuranceBase(unemploymentInsuranceBase);
        profile.setUnemploymentInsuranceRate(resolveInsuranceRate(
                requestDTO.getUnemploymentInsuranceRate(), profile.getUnemploymentInsuranceRate(), DEFAULT_UNEMPLOYMENT_INSURANCE_RATE));
        profile.setSocialInsuranceBase(resolveCompatibleSocialInsuranceBase(
                requestDTO.getSocialInsuranceBase(), pensionInsuranceBase, medicalInsuranceBase, unemploymentInsuranceBase));
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
     * 保存薪资模板项。
     * @param templateId 模板ID
     * @param items 模板项列表
     */
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

    /**
     * 列出薪资模板项。
     * @param templateIds 模板ID列表
     * @return 模板项列表
     */
    private List<SalaryTemplateItemEntity> listTemplateItems(List<Long> templateIds) {
        if (CollUtil.isEmpty(templateIds)) {
            return List.of();
        }
        return salaryTemplateItemMapper.selectList(Wrappers.lambdaQuery(SalaryTemplateItemEntity.class)
                .in(SalaryTemplateItemEntity::getTemplateId, templateIds)
                .orderByAsc(SalaryTemplateItemEntity::getSortNo));
    }

    /**
     * 获取薪资账套。
     * @param id 薪资账套ID
     * @return 薪资账套
     */
    private SalaryTemplateEntity getTemplateRequired(Long id) {
        SalaryTemplateEntity entity = salaryTemplateMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "薪资账套不存在");
        }
        return entity;
    }

    /**
     * 获取员工。
     * @param employeeId 员工ID
     * @return 员工
     */
    private SalaryEmployeeSnapshotEntity getEmployeeRequired(Long employeeId) {
        SalaryEmployeeSnapshotEntity employee = employeeSnapshotMapper.selectById(employeeId);
        if (employee == null || Objects.equals(employee.getIsDeleted(), 1)) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "员工不存在");
        }
        return employee;
    }

    /**
     * 薪资模板项转VO。
     * @param profile 薪资模板项
     * @param employee 员工
     * @return 薪资模板项VO
     */
    private EmployeeSalaryProfileVO toProfileVO(EmployeeSalaryProfileEntity profile, SalaryEmployeeSnapshotEntity employee) {
        SalaryTemplateEntity template = profile.getTemplateId() == null ? null : salaryTemplateMapper.selectById(profile.getTemplateId());
        return EmployeeSalaryProfileVO.builder()
                .id(profile.getId())
                .employeeId(profile.getEmployeeId())
                .employeeNo(employee.getEmployeeNo())
                .employeeName(employee.getEmployeeName())
                .templateId(profile.getTemplateId())
                .templateName(template == null ? null : template.getTemplateName())
                .baseSalary(profile.getBaseSalary())
                .allowance(profile.getAllowance())
                .performanceBase(profile.getPerformanceBase())
                .socialInsuranceBase(resolveCompatibleSocialInsuranceBase(
                        profile.getSocialInsuranceBase(),
                        profile.getPensionInsuranceBase(),
                        profile.getMedicalInsuranceBase(),
                        profile.getUnemploymentInsuranceBase()))
                .pensionInsuranceBase(resolveInsuranceBase(
                        profile.getPensionInsuranceBase(), profile.getSocialInsuranceBase(), null))
                .pensionInsuranceRate(resolveInsuranceRate(
                        profile.getPensionInsuranceRate(), null, DEFAULT_PENSION_INSURANCE_RATE))
                .medicalInsuranceBase(resolveInsuranceBase(
                        profile.getMedicalInsuranceBase(), profile.getSocialInsuranceBase(), null))
                .medicalInsuranceRate(resolveInsuranceRate(
                        profile.getMedicalInsuranceRate(), null, DEFAULT_MEDICAL_INSURANCE_RATE))
                .unemploymentInsuranceBase(resolveInsuranceBase(
                        profile.getUnemploymentInsuranceBase(), profile.getSocialInsuranceBase(), null))
                .unemploymentInsuranceRate(resolveInsuranceRate(
                        profile.getUnemploymentInsuranceRate(), null, DEFAULT_UNEMPLOYMENT_INSURANCE_RATE))
                .housingFundBase(profile.getHousingFundBase())
                .bankName(profile.getBankName())
                .bankAccountMasked(maskBankAccount(profile.getBankAccount()))
                .effectiveDate(profile.getEffectiveDate())
                .remark(profile.getRemark())
                .build();
    }

    /**
     * 薪资账套转VO。
     * @param entity 薪资账套
     * @param items 模板项列表
     * @return 薪资账套VO
     */
    private SalaryTemplatePageVO toTemplateVO(SalaryTemplateEntity entity, List<SalaryTemplateItemEntity> items) {
        return SalaryTemplatePageVO.builder()
                .id(entity.getId())
                .templateName(entity.getTemplateName())
                .templateCode(entity.getTemplateCode())
                .scopeType(entity.getScopeType())
                .scopeValue(entity.getScopeValue())
                .scopeName(resolveScopeName(entity.getScopeType()))
                .effectiveDate(entity.getEffectiveDate())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .itemCount(items.size())
                .items(items.stream().map(this::toTemplateItemVO).toList())
                .build();
    }

    /**
     * 薪资模板项转VO。
     * @param entity 薪资模板项
     * @return 薪资模板项VO
     */
    private SalaryTemplateItemVO toTemplateItemVO(SalaryTemplateItemEntity entity) {
        return SalaryTemplateItemVO.builder()
                .id(entity.getId())
                .itemCode(entity.getItemCode())
                .itemName(entity.getItemName())
                .category(entity.getCategory())
                .calcRule(entity.getCalcRule())
                .defaultValue(entity.getDefaultValue())
                .sortNo(entity.getSortNo())
                .build();
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
     * 追加薪资账套适用范围查询条件。
     *
     * @param wrapper 查询条件构造器
     * @param scope   适用范围查询值
     * 本方法使用的工具类: StrUtil(hutool),LambdaQueryWrapper(MyBatis-Plus)
     */
    private void appendTemplateScopeCondition(LambdaQueryWrapper<SalaryTemplateEntity> wrapper, String scope) {
        if (StrUtil.isBlank(scope)) {
            return;
        }
        String scopeText = scope.trim();
        String normalized = normalizeTemplateScopeFilter(scopeText);
        if (normalized != null) {
            wrapper.eq(SalaryTemplateEntity::getScopeType, normalized);
            return;
        }
        wrapper.like(SalaryTemplateEntity::getScopeValue, scopeText);
    }

    /**
     * 规范化薪资账套适用范围查询值。
     *
     * @param scope 适用范围查询值
     * @return 可按范围类型查询的标准值，非范围类型时返回 null
     * 本方法使用的工具类: Set(JDK)
     */
    private String normalizeTemplateScopeFilter(String scope) {
        String normalized = scope.trim().toUpperCase();
        if ("DEPARTMENT".equals(normalized)) {
            return "DEPT";
        }
        if ("LEVEL".equals(normalized)) {
            return "JOB_LEVEL";
        }
        if (Set.of("ALL", "DEPT", "EMPLOYEE", "JOB_LEVEL").contains(normalized)) {
            return normalized;
        }
        return null;
    }

    /**
     * 解析薪资账套适用范围展示名称。
     *
     * @param scopeType 适用范围类型
     * @return 适用范围展示名称
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private String resolveScopeName(String scopeType) {
        if (StrUtil.isBlank(scopeType)) {
            return "全部";
        }
        return switch (scopeType.trim().toUpperCase()) {
            case "ALL" -> "全部";
            case "DEPT" -> "部门";
            case "EMPLOYEE" -> "员工";
            case "JOB_LEVEL" -> "职级";
            default -> scopeType;
        };
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

    /**
     * 银行账号脱敏。
     * @param bankAccount 银行账号
     * @return 脱敏后的银行账号
     */
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
}
