package com.hrms.business.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.employee.dto.EmployeeCreateDTO;
import com.hrms.business.employee.dto.EmployeeQueryDTO;
import com.hrms.business.employee.dto.EmployeeUpdateDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.enums.EmploymentStatusEnum;
import com.hrms.business.employee.mapper.EmployeeMapper;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.employee.vo.EmployeeDetailVO;
import com.hrms.business.employee.vo.EmployeeGenNoVO;
import com.hrms.business.employee.util.AesEncryptUtil;
import com.hrms.business.employee.util.DesensitizationUtil;
import com.hrms.business.employee.vo.EmployeeListVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import com.hrms.system.auth.dto.UserCreateDTO;
import com.hrms.system.auth.service.FieldPermissionService;
import com.hrms.system.auth.service.UserService;
import com.hrms.system.auth.vo.FieldPermissionVO;
import com.hrms.system.auth.vo.UserCreateResultVO;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.vo.DeptDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final FieldPermissionService fieldPermissionService;
    private final DeptService deptService;
    private final UserService userService;

    @Override
    public PageResult<EmployeeListVO> listEmployees(EmployeeQueryDTO queryDTO) {
        // 构建分页参数
        Page<EmployeeEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<EmployeeEntity> wrapper = Wrappers.lambdaQuery();

        // 关键词搜索（姓名/工号/手机号）
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(EmployeeEntity::getEmployeeName, queryDTO.getKeyword())
                    .or()
                    .like(EmployeeEntity::getEmployeeNo, queryDTO.getKeyword())
                    .or()
                    .like(EmployeeEntity::getPhone, queryDTO.getKeyword()));
        }

        // 部门筛选
        if (queryDTO.getDeptIds() != null && !queryDTO.getDeptIds().isEmpty()) {
            wrapper.in(EmployeeEntity::getDeptId, queryDTO.getDeptIds());
        }

        // 在职状态筛选
        if (queryDTO.getEmploymentStatus() != null && !queryDTO.getEmploymentStatus().isEmpty()) {
            wrapper.in(EmployeeEntity::getEmploymentStatus, queryDTO.getEmploymentStatus());
        }

        // 职级筛选
        if (queryDTO.getJobLevel() != null && !queryDTO.getJobLevel().isEmpty()) {
            wrapper.eq(EmployeeEntity::getJobLevel, queryDTO.getJobLevel());
        }

        // 入职日期范围
        if (queryDTO.getHireDateStart() != null) {
            wrapper.ge(EmployeeEntity::getHireDate, queryDTO.getHireDateStart());
        }
        if (queryDTO.getHireDateEnd() != null) {
            wrapper.le(EmployeeEntity::getHireDate, queryDTO.getHireDateEnd());
        }

        // 执行查询
        Page<EmployeeEntity> resultPage = employeeMapper.selectPage(page, wrapper);

        // 转换为 VO
        List<EmployeeListVO> records = resultPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        PageResult<EmployeeListVO> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPageNum((int) resultPage.getCurrent());
        pageResult.setPageSize((int) resultPage.getSize());
        pageResult.setPages((int) resultPage.getPages());

        return pageResult;
    }

    @Override
    public EmployeeDetailVO getEmployeeDetail(Long id) {
        EmployeeEntity entity = employeeMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
        EmployeeDetailVO vo = convertToDetailVO(entity);
        // 填充字段权限
        vo.setFieldPermissions(buildFieldPermissions());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeEntity createEmployee(EmployeeCreateDTO createDTO) {
        // 检查手机号是否已存在
        EmployeeEntity existing = employeeMapper.selectOne(
                Wrappers.<EmployeeEntity>lambdaQuery()
                        .eq(EmployeeEntity::getPhone, createDTO.getPhone())
        );
        if (existing != null) {
            throw new GlobalException(ErrorCode.EMPLOYEE_PHONE_EXISTS);
        }

        // 创建员工实体
        EmployeeEntity entity = new EmployeeEntity();
        entity.setEmployeeName(createDTO.getEmployeeName());
        entity.setGender(createDTO.getGender());
        entity.setPhone(createDTO.getPhone());
        entity.setEmail(createDTO.getEmail());
        entity.setDeptId(createDTO.getDeptId());
        entity.setPostId(createDTO.getPostId());
        entity.setJobLevel(createDTO.getJobLevel());
        entity.setLeaderId(createDTO.getLeaderId());
        entity.setWorkLocation(createDTO.getWorkLocation());
        entity.setHireType(createDTO.getHireType());
        entity.setHireDate(createDTO.getHireDate());
        entity.setProbationMonth(createDTO.getProbationMonth());
        entity.setProbationSalaryRatio(createDTO.getProbationSalaryRatio());
        entity.setContractType(createDTO.getContractType());
        entity.setContractExpireDate(createDTO.getContractExpireDate());
        entity.setSalaryTemplateId(createDTO.getSalaryTemplateId());
        entity.setBaseSalary(createDTO.getBaseSalary());
        entity.setIdCardNo(AesEncryptUtil.encrypt(createDTO.getIdCardNo()));
        entity.setBirthday(createDTO.getBirthday());
        entity.setDomicileAddress(createDTO.getDomicileAddress());
        entity.setCurrentAddress(createDTO.getCurrentAddress());
        entity.setBankAccount(AesEncryptUtil.encrypt(createDTO.getBankAccount()));
        entity.setBankName(createDTO.getBankName());
        entity.setEmergencyContact(createDTO.getEmergencyContact());
        entity.setEmergencyPhone(createDTO.getEmergencyPhone());
        entity.setRemark(createDTO.getRemark());

        // 设置在职状态为试用期
        entity.setEmploymentStatus(EmploymentStatusEnum.PROBATION.getCode());

        // 生成工号：查询部门编码，按规范生成
        DeptDetailVO dept = deptService.getDeptById(createDTO.getDeptId());
        if (dept == null || dept.getDeptCode() == null || dept.getDeptCode().isEmpty()) {
            throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "部门编码不存在，无法生成工号");
        }
        EmployeeGenNoVO genNoVO = generateEmployeeNo(dept.getDeptCode());
        entity.setEmployeeNo(genNoVO.getEmployeeNo());

        // 保存员工记录
        employeeMapper.insert(entity);

        // 创建系统账号
        createUserAccount(entity);

        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeEntity updateEmployee(Long id, EmployeeUpdateDTO updateDTO) {
        EmployeeEntity entity = employeeMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        // 更新字段
        if (updateDTO.getEmployeeName() != null) {
            entity.setEmployeeName(updateDTO.getEmployeeName());
        }
        if (updateDTO.getGender() != null) {
            entity.setGender(updateDTO.getGender());
        }
        if (updateDTO.getPhone() != null) {
            entity.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getEmail() != null) {
            entity.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getDeptId() != null) {
            entity.setDeptId(updateDTO.getDeptId());
        }
        if (updateDTO.getPostId() != null) {
            entity.setPostId(updateDTO.getPostId());
        }
        if (updateDTO.getJobLevel() != null) {
            entity.setJobLevel(updateDTO.getJobLevel());
        }
        if (updateDTO.getLeaderId() != null) {
            entity.setLeaderId(updateDTO.getLeaderId());
        }
        if (updateDTO.getWorkLocation() != null) {
            entity.setWorkLocation(updateDTO.getWorkLocation());
        }
        if (updateDTO.getHireType() != null) {
            entity.setHireType(updateDTO.getHireType());
        }
        if (updateDTO.getEmploymentStatus() != null) {
            entity.setEmploymentStatus(updateDTO.getEmploymentStatus());
        }
        if (updateDTO.getHireDate() != null) {
            entity.setHireDate(updateDTO.getHireDate());
        }
        if (updateDTO.getProbationMonth() != null) {
            entity.setProbationMonth(updateDTO.getProbationMonth());
        }
        if (updateDTO.getProbationSalaryRatio() != null) {
            entity.setProbationSalaryRatio(updateDTO.getProbationSalaryRatio());
        }
        if (updateDTO.getContractType() != null) {
            entity.setContractType(updateDTO.getContractType());
        }
        if (updateDTO.getContractExpireDate() != null) {
            entity.setContractExpireDate(updateDTO.getContractExpireDate());
        }
        if (updateDTO.getSalaryTemplateId() != null) {
            entity.setSalaryTemplateId(updateDTO.getSalaryTemplateId());
        }
        if (updateDTO.getBaseSalary() != null) {
            entity.setBaseSalary(updateDTO.getBaseSalary());
        }
        if (updateDTO.getIdCardNo() != null) {
            entity.setIdCardNo(AesEncryptUtil.encrypt(updateDTO.getIdCardNo()));
        }
        if (updateDTO.getBirthday() != null) {
            entity.setBirthday(updateDTO.getBirthday());
        }
        if (updateDTO.getDomicileAddress() != null) {
            entity.setDomicileAddress(updateDTO.getDomicileAddress());
        }
        if (updateDTO.getCurrentAddress() != null) {
            entity.setCurrentAddress(updateDTO.getCurrentAddress());
        }
        if (updateDTO.getBankAccount() != null) {
            entity.setBankAccount(AesEncryptUtil.encrypt(updateDTO.getBankAccount()));
        }
        if (updateDTO.getBankName() != null) {
            entity.setBankName(updateDTO.getBankName());
        }
        if (updateDTO.getEmergencyContact() != null) {
            entity.setEmergencyContact(updateDTO.getEmergencyContact());
        }
        if (updateDTO.getEmergencyPhone() != null) {
            entity.setEmergencyPhone(updateDTO.getEmergencyPhone());
        }
        if (updateDTO.getRemark() != null) {
            entity.setRemark(updateDTO.getRemark());
        }

        employeeMapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeEntity patchEmployee(Long id, EmployeeUpdateDTO updateDTO) {
        // PATCH 逻辑与 PUT 相同，都是更新非空字段
        return updateEmployee(id, updateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(Long id) {
        EmployeeEntity entity = employeeMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        // 校验：仅限试用期员工
        if (entity.getEmploymentStatus() != EmploymentStatusEnum.PROBATION.getCode()) {
            throw new GlobalException(ErrorCode.EMPLOYEE_CANNOT_DELETE);
        }

        // TODO: 校验是否存在业务记录（合同、考勤、薪资等）

        // 逻辑删除
        employeeMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeGenNoVO generateEmployeeNo(String deptCode) {
        // 获取当前年份
        String year = String.valueOf(LocalDate.now().getYear());

        // 查询当前年份该部门的最大工号
        LambdaQueryWrapper<EmployeeEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.likeRight(EmployeeEntity::getEmployeeNo, year + deptCode);
        wrapper.orderByDesc(EmployeeEntity::getEmployeeNo);
        wrapper.last("FOR UPDATE");

        List<EmployeeEntity> employees = employeeMapper.selectList(wrapper);

        int nextSeq = 1;
        if (!employees.isEmpty()) {
            String lastNo = employees.get(0).getEmployeeNo();
            // 提取序号部分（最后3位）
            String seqStr = lastNo.substring(lastNo.length() - 3);
            try {
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析工号序号失败: {}", lastNo);
            }
        }

        // 格式化为3位序号
        String seqStr = String.format("%03d", nextSeq);
        String employeeNo = year + deptCode + seqStr;

        EmployeeGenNoVO vo = new EmployeeGenNoVO();
        vo.setEmployeeNo(employeeNo);
        return vo;
    }

    @Override
    public EmployeeEntity getEmployeeBrief(Long id) {
        return employeeMapper.selectById(id);
    }

    @Override
    public List<EmployeeEntity> getEmployeesByDept(Long deptId) {
        LambdaQueryWrapper<EmployeeEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(EmployeeEntity::getDeptId, deptId);
        // 只查询在职员工（试用期和正式）
        wrapper.in(EmployeeEntity::getEmploymentStatus,
                EmploymentStatusEnum.PROBATION.getCode(),
                EmploymentStatusEnum.FORMAL.getCode());
        return employeeMapper.selectList(wrapper);
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为列表 VO
     */
    private EmployeeListVO convertToListVO(EmployeeEntity entity) {
        EmployeeListVO vo = new EmployeeListVO();
        vo.setId(entity.getId());
        vo.setEmployeeNo(entity.getEmployeeNo());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setGender(entity.getGender());
        vo.setPhone(DesensitizationUtil.desensitizePhone(entity.getPhone()));
        vo.setDeptId(entity.getDeptId());
        vo.setJobLevel(entity.getJobLevel());
        vo.setEmploymentStatus(entity.getEmploymentStatus());
        vo.setHireDate(entity.getHireDate());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /**
     * 转换为详情 VO
     */
    private EmployeeDetailVO convertToDetailVO(EmployeeEntity entity) {
        EmployeeDetailVO vo = new EmployeeDetailVO();
        vo.setId(entity.getId());
        vo.setEmployeeNo(entity.getEmployeeNo());
        vo.setUserId(entity.getUserId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setGender(entity.getGender());
        vo.setPhone(DesensitizationUtil.desensitizePhone(entity.getPhone()));
        vo.setEmail(entity.getEmail());
        vo.setIdCardNo(DesensitizationUtil.desensitizeIdCardNo(AesEncryptUtil.decrypt(entity.getIdCardNo())));
        vo.setBirthday(entity.getBirthday());
        vo.setDomicileAddress(entity.getDomicileAddress());
        vo.setCurrentAddress(entity.getCurrentAddress());
        vo.setDeptId(entity.getDeptId());
        vo.setPostId(entity.getPostId());
        vo.setJobLevel(entity.getJobLevel());
        vo.setLeaderId(entity.getLeaderId());
        vo.setWorkLocation(entity.getWorkLocation());
        vo.setHireType(entity.getHireType());
        vo.setEmploymentStatus(entity.getEmploymentStatus());
        vo.setHireDate(entity.getHireDate());
        vo.setProbationMonth(entity.getProbationMonth());
        vo.setProbationSalaryRatio(entity.getProbationSalaryRatio());
        vo.setContractType(entity.getContractType());
        vo.setContractExpireDate(entity.getContractExpireDate());
        vo.setSalaryTemplateId(entity.getSalaryTemplateId());
        vo.setBaseSalary(entity.getBaseSalary());
        vo.setBankAccount(DesensitizationUtil.desensitizeBankAccount(AesEncryptUtil.decrypt(entity.getBankAccount())));
        vo.setBankName(entity.getBankName());
        vo.setEmergencyContact(entity.getEmergencyContact());
        vo.setEmergencyPhone(DesensitizationUtil.desensitizePhone(entity.getEmergencyPhone()));
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /**
     * 构建字段权限配置
     * <p>
     * 调用 auth 模块的 FieldPermissionService 获取当前用户对员工档案的字段权限
     * </p>
     *
     * @return 字段权限配置
     */
    private java.util.Map<String, java.util.List<String>> buildFieldPermissions() {
        try {
            java.util.List<Long> roleIds = SecurityContextHolder.getRoleIds();
            FieldPermissionVO fp = fieldPermissionService.getFieldPermissions("employee", roleIds);

            java.util.Map<String, java.util.List<String>> result = new java.util.HashMap<>();
            result.put("viewableFields", fp.getViewableFields());
            result.put("editableFields", fp.getEditableFields());
            result.put("flowRequiredFields", fp.getFlowRequiredFields());
            return result;
        } catch (Exception e) {
            log.warn("获取字段权限失败，返回默认权限", e);
            // 默认返回全部可见可编辑
            return java.util.Map.of(
                    "viewableFields", java.util.List.of("*"),
                    "editableFields", java.util.List.of("*"),
                    "flowRequiredFields", java.util.List.of()
            );
        }
    }

    /**
     * 创建系统账号
     * <p>
     * 新增员工时自动创建系统账号，登录账号=手机号，初始密码随机生成
     * </p>
     *
     * @param entity 员工实体
     */
    private void createUserAccount(EmployeeEntity entity) {
        try {
            // 生成随机初始密码
            String initialPassword = generateRandomPassword();

            // 构建创建用户 DTO
            UserCreateDTO userCreateDTO = new UserCreateDTO();
            userCreateDTO.setUsername(entity.getPhone());           // 登录账号 = 手机号
            userCreateDTO.setPassword(initialPassword);              // 初始密码
            userCreateDTO.setRealName(entity.getEmployeeName());     // 真实姓名
            userCreateDTO.setPhone(entity.getPhone());               // 手机号
            userCreateDTO.setEmail(entity.getEmail());               // 邮箱
            userCreateDTO.setEmployeeId(entity.getId());             // 关联员工ID

            // 调用用户服务创建账号
            UserCreateResultVO result = userService.createUser(userCreateDTO);

            // 回填 user_id 到员工记录
            entity.setUserId(result.getId());
            employeeMapper.updateById(entity);

            log.info("员工 [{}] 系统账号创建成功，userId={}", entity.getEmployeeName(), result.getId());
        } catch (Exception e) {
            log.error("创建员工作业账号失败，employeeId={}", entity.getId(), e);
            // 不阻断主流程，记录日志即可
        }
    }

    /**
     * 生成随机密码
     *
     * @return 8位随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}
