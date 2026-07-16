package com.hrms.business.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.employee.convert.EmployeeConvert;
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
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.DeptDetailVO;
import com.hrms.system.organization.vo.PostVO;
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
    private final PostService postService;
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
        checkPhoneUnique(createDTO.getPhone(), null);

        // 校验部门是否存在
        validateDeptExists(createDTO.getDeptId());

        // 校验职位是否存在（如果传了职位ID）
        if (createDTO.getPostId() != null) {
            validatePostExists(createDTO.getPostId());
        }

        // 使用 MapStruct 转换 DTO 为 Entity
        EmployeeEntity entity = EmployeeConvert.INSTANCE.toEntity(createDTO);

        // 加密敏感字段
        entity.setIdCardNo(AesEncryptUtil.encrypt(createDTO.getIdCardNo()));
        entity.setBankAccount(AesEncryptUtil.encrypt(createDTO.getBankAccount()));

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

        // 校验手机号唯一性（如果变更了手机号）
        if (updateDTO.getPhone() != null && !updateDTO.getPhone().equals(entity.getPhone())) {
            checkPhoneUnique(updateDTO.getPhone(), id);
        }

        // 校验部门是否存在（如果变更了部门）
        if (updateDTO.getDeptId() != null && !updateDTO.getDeptId().equals(entity.getDeptId())) {
            validateDeptExists(updateDTO.getDeptId());
        }

        // 校验职位是否存在（如果变更了职位）
        if (updateDTO.getPostId() != null && !updateDTO.getPostId().equals(entity.getPostId())) {
            validatePostExists(updateDTO.getPostId());
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
        EmployeeEntity entity = employeeMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        // PATCH 只允许更新部分字段（状态、联系方式、地址等）
        // 不允许更新：姓名、部门、职位、职级、工号、薪资等核心字段
        if (updateDTO.getPhone() != null) {
            entity.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getEmail() != null) {
            entity.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getEmploymentStatus() != null) {
            entity.setEmploymentStatus(updateDTO.getEmploymentStatus());
        }
        if (updateDTO.getCurrentAddress() != null) {
            entity.setCurrentAddress(updateDTO.getCurrentAddress());
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
    public void deleteEmployee(Long id) {
        EmployeeEntity entity = employeeMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        // 校验：仅限试用期员工
        if (entity.getEmploymentStatus() != EmploymentStatusEnum.PROBATION.getCode()) {
            throw new GlobalException(ErrorCode.EMPLOYEE_CANNOT_DELETE);
        }

        // 校验：是否存在合同记录
        Long contractCount = employeeMapper.countContractsByEmployeeId(id);
        if (contractCount != null && contractCount > 0) {
            throw new GlobalException(ErrorCode.EMPLOYEE_CANNOT_DELETE, "该员工存在合同记录，无法删除");
        }

        // 校验：是否存在考勤记录
        Long attendanceCount = employeeMapper.countAttendanceByEmployeeId(id);
        if (attendanceCount != null && attendanceCount > 0) {
            throw new GlobalException(ErrorCode.EMPLOYEE_CANNOT_DELETE, "该员工存在考勤记录，无法删除");
        }

        // 校验：是否存在薪资记录
        Long salaryCount = employeeMapper.countSalaryByEmployeeId(id);
        if (salaryCount != null && salaryCount > 0) {
            throw new GlobalException(ErrorCode.EMPLOYEE_CANNOT_DELETE, "该员工存在薪资记录，无法删除");
        }

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

    @Override
    public List<EmployeeEntity> getEmployeesByPostId(Long postId) {
        LambdaQueryWrapper<EmployeeEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(EmployeeEntity::getPostId, postId);
        // 只查询在职员工（试用期和正式）
        wrapper.in(EmployeeEntity::getEmploymentStatus,
                EmploymentStatusEnum.PROBATION.getCode(),
                EmploymentStatusEnum.FORMAL.getCode());
        return employeeMapper.selectList(wrapper);
    }

    @Override
    public boolean hasEmployeesInDept(Long deptId) {
        List<EmployeeEntity> employees = getEmployeesByDept(deptId);
        return !employees.isEmpty();
    }

    @Override
    public boolean hasEmployeesInPost(Long postId) {
        List<EmployeeEntity> employees = getEmployeesByPostId(postId);
        return !employees.isEmpty();
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为列表 VO
     */
    private EmployeeListVO convertToListVO(EmployeeEntity entity) {
        EmployeeListVO vo = EmployeeConvert.INSTANCE.toListVO(entity);

        // 填充部门名称
        if (entity.getDeptId() != null) {
            try {
                DeptDetailVO dept = deptService.getDeptById(entity.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            } catch (Exception e) {
                log.warn("获取部门名称失败，deptId={}", entity.getDeptId());
            }
        }

        // 填充职位名称
        if (entity.getPostId() != null) {
            try {
                PostVO post = postService.getPostById(entity.getPostId());
                if (post != null) {
                    vo.setPostName(post.getPostName());
                }
            } catch (Exception e) {
                log.warn("获取职位名称失败，postId={}", entity.getPostId());
            }
        }

        // 填充汇报人姓名
        if (entity.getLeaderId() != null) {
            try {
                EmployeeEntity leader = employeeMapper.selectById(entity.getLeaderId());
                if (leader != null) {
                    vo.setLeaderName(leader.getEmployeeName());
                }
            } catch (Exception e) {
                log.warn("获取汇报人姓名失败，leaderId={}", entity.getLeaderId());
            }
        }

        // 脱敏手机号
        vo.setPhone(DesensitizationUtil.desensitizePhone(entity.getPhone()));

        return vo;
    }

    /**
     * 转换为详情 VO
     */
    private EmployeeDetailVO convertToDetailVO(EmployeeEntity entity) {
        EmployeeDetailVO vo = EmployeeConvert.INSTANCE.toDetailVO(entity);

        // 填充部门名称
        if (entity.getDeptId() != null) {
            try {
                DeptDetailVO dept = deptService.getDeptById(entity.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            } catch (Exception e) {
                log.warn("获取部门名称失败，deptId={}", entity.getDeptId());
            }
        }

        // 填充职位名称
        if (entity.getPostId() != null) {
            try {
                PostVO post = postService.getPostById(entity.getPostId());
                if (post != null) {
                    vo.setPostName(post.getPostName());
                }
            } catch (Exception e) {
                log.warn("获取职位名称失败，postId={}", entity.getPostId());
            }
        }

        // 填充汇报人姓名
        if (entity.getLeaderId() != null) {
            try {
                EmployeeEntity leader = employeeMapper.selectById(entity.getLeaderId());
                if (leader != null) {
                    vo.setLeaderName(leader.getEmployeeName());
                }
            } catch (Exception e) {
                log.warn("获取汇报人姓名失败，leaderId={}", entity.getLeaderId());
            }
        }

        // 脱敏敏感字段
        vo.setPhone(DesensitizationUtil.desensitizePhone(entity.getPhone()));
        vo.setIdCardNo(DesensitizationUtil.desensitizeIdCardNo(AesEncryptUtil.decrypt(entity.getIdCardNo())));
        vo.setBankAccount(DesensitizationUtil.desensitizeBankAccount(AesEncryptUtil.decrypt(entity.getBankAccount())));
        vo.setEmergencyPhone(DesensitizationUtil.desensitizePhone(entity.getEmergencyPhone()));

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
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 校验手机号唯一性
     *
     * @param phone 手机号
     * @param excludeId 排除的员工ID（更新时使用）
     */
    private void checkPhoneUnique(String phone, Long excludeId) {
        EmployeeEntity existing = employeeMapper.selectOne(
                Wrappers.<EmployeeEntity>lambdaQuery()
                        .eq(EmployeeEntity::getPhone, phone)
        );
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new GlobalException(ErrorCode.EMPLOYEE_PHONE_EXISTS);
        }
    }

    /**
     * 校验部门是否存在
     *
     * @param deptId 部门ID
     */
    private void validateDeptExists(Long deptId) {
        if (deptId == null) {
            return;
        }
        DeptDetailVO dept = deptService.getDeptById(deptId);
        if (dept == null) {
            throw new GlobalException(ErrorCode.EMPLOYEE_DEPT_NOT_FOUND);
        }
    }

    /**
     * 校验职位是否存在
     *
     * @param postId 职位ID
     */
    private void validatePostExists(Long postId) {
        if (postId == null) {
            return;
        }
        PostVO post = postService.getPostById(postId);
        if (post == null) {
            throw new GlobalException(ErrorCode.EMPLOYEE_POST_NOT_FOUND);
        }
    }

}
