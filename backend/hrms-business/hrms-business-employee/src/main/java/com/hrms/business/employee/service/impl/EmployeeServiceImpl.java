package com.hrms.business.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.employee.convert.EmployeeConvert;
import com.hrms.business.employee.dto.EmployeeApprovalSyncUpdateDTO;
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
import org.springframework.dao.DuplicateKeyException;
import com.hrms.system.auth.dto.UserCreateDTO;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.service.FieldPermissionService;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.auth.service.UserService;
import com.hrms.system.auth.vo.FieldPermissionVO;
import com.hrms.system.auth.vo.UserCreateResultVO;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.DeptDetailVO;
import com.hrms.system.organization.vo.PostVO;
import com.hrms.business.employee.event.EmployeeChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import java.util.concurrent.CompletableFuture;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import cn.hutool.json.JSONUtil;
/**
 * 员工服务实现
 */
@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final FieldPermissionService fieldPermissionService;
    private final DeptService deptService;
    private final PostService postService;
    private final UserService userService;
    private final RoleService roleService;
    private final ApplicationEventPublisher eventPublisher;

    /** 独立事务模板，用于创建系统账号（失败不回滚主事务） */
    private final TransactionTemplate transactionTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    /** ???? */
    private static final String CACHE_PREFIX = "employee:list:";
    private static final int CACHE_MAX_PAGES = 9;
    private static final long CACHE_TTL_MINUTES = 30;
    private static final long EMPTY_TTL_MINUTES = 1;
    private static final String EMPTY_MARKER = "__EMPTY__";

    public EmployeeServiceImpl(
            EmployeeMapper employeeMapper,
            FieldPermissionService fieldPermissionService,
            DeptService deptService,
            PostService postService,
            UserService userService,
            RoleService roleService,
            ApplicationEventPublisher eventPublisher,
                        StringRedisTemplate stringRedisTemplate,
            PlatformTransactionManager transactionManager) {
        this.employeeMapper = employeeMapper;
        this.fieldPermissionService = fieldPermissionService;
        this.deptService = deptService;
        this.postService = postService;
        this.userService = userService;
        this.roleService = roleService;
        this.eventPublisher = eventPublisher;
                this.stringRedisTemplate = stringRedisTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public PageResult<EmployeeListVO> listEmployees(EmployeeQueryDTO queryDTO) {
        // ???????lastId ????????????????O(1) ???
        if (queryDTO.getLastId() != null) {
            return listEmployeesByCursor(queryDTO);
        }

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 20;

        // ?10???? Redis ??
        if (pageNum <= CACHE_MAX_PAGES && stringRedisTemplate != null) {
            Long userId = SecurityContextHolder.getUserId();
            String cacheKey = buildListCacheKeyWithUserInfo(queryDTO, pageNum, userId);
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                if (EMPTY_MARKER.equals(cached)) {
                    return PageResult.of(List.of(), 0, pageNum, pageSize);
                }
                return JSONUtil.toBean(cached, PageResult.class);
            }

            // ?1?????????????10????????
            if (pageNum == 1) {
                return prefetchAndCachePages(queryDTO, pageSize);
            }
        }

        PageResult<EmployeeListVO> result = listEmployeesByOffset(queryDTO);

        // ????????
        if (pageNum <= CACHE_MAX_PAGES && stringRedisTemplate != null) {
            Long userId = SecurityContextHolder.getUserId();
            String cacheKey = buildListCacheKeyWithUserInfo(queryDTO, pageNum, userId);
            if (result.getRecords() == null || result.getRecords().isEmpty()) {
                stringRedisTemplate.opsForValue().set(cacheKey, EMPTY_MARKER, EMPTY_TTL_MINUTES, TimeUnit.MINUTES);
            } else {
                stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            }
        }

        return result;
    }

    /**
     * 传统偏移分页查询员工列表
     */
    private PageResult<EmployeeListVO> listEmployeesByOffset(EmployeeQueryDTO queryDTO) {
        Page<EmployeeEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<EmployeeEntity> wrapper = buildEmployeeWrapper(queryDTO);
        if (wrapper == null) {
            return PageResult.of(List.of(), 0, queryDTO.getPageNum(), queryDTO.getPageSize());
        }
        Page<EmployeeEntity> resultPage = employeeMapper.selectPage(page, wrapper);
        return buildPageResult(resultPage, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 构建员工分页查询的 QueryWrapper（含数据权限和业务过滤）
     *
     * @param queryDTO 查询参数
     * @return QueryWrapper，返回 null 表示数据权限无交集，应返回空结果
     */
    private LambdaQueryWrapper<EmployeeEntity> buildEmployeeWrapper(EmployeeQueryDTO queryDTO) {
        LambdaQueryWrapper<EmployeeEntity> wrapper = Wrappers.lambdaQuery();

        // ========== 数据权限过滤 ==========
        List<Long> accessibleDeptIds = resolveAccessibleDeptIds();
        if (accessibleDeptIds != null) {
            if (accessibleDeptIds.isEmpty()) {
                wrapper.eq(EmployeeEntity::getCreateBy, SecurityContextHolder.getUserId());
            } else {
                if (queryDTO.getDeptIds() != null && !queryDTO.getDeptIds().isEmpty()) {
                    List<Long> intersected = queryDTO.getDeptIds().stream()
                            .filter(accessibleDeptIds::contains)
                            .collect(Collectors.toList());
                    if (intersected.isEmpty()) {
                        return null;
                    }
                    wrapper.in(EmployeeEntity::getDeptId, intersected);
                } else {
                    wrapper.in(EmployeeEntity::getDeptId, accessibleDeptIds);
                }
            }
        }

        // 关键词搜索（姓名/工号/手机号）
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(EmployeeEntity::getEmployeeName, queryDTO.getKeyword())
                    .or()
                    .like(EmployeeEntity::getEmployeeNo, queryDTO.getKeyword())
                    .or()
                    .like(EmployeeEntity::getPhone, queryDTO.getKeyword()));
        }

        // 部门筛选（全量权限且前端传了 deptIds 时生效）
        if (queryDTO.getDeptIds() != null && !queryDTO.getDeptIds().isEmpty()
                && accessibleDeptIds == null) {
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

        return wrapper;
    }

    /**
     * 构建分页结果（从 MyBatis-Plus Page 转换为 PageResult）
     *
     * @param resultPage MyBatis-Plus 分页结果
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @return PageResult
     */
    private PageResult<EmployeeListVO> buildPageResult(Page<EmployeeEntity> resultPage, int pageNum, int pageSize) {
        List<EmployeeListVO> records = resultPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        PageResult<EmployeeListVO> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);
        pageResult.setPages((int) resultPage.getPages());
        return pageResult;
    }

    /**
     * 预取前10页数据并全部缓存到 Redis
     * 一次数据库查询获取 pageSize * 10 条记录，在内存中拆分后分别缓存每个页
     *
     * @param queryDTO 查询参数
     * @param pageSize 每页条数
     * @return 第1页结果
     * 本方法使用的工具类：StringRedisTemplate(spring-data-redis), JSONUtil(hutool)
     */
    private PageResult<EmployeeListVO> prefetchAndCachePages(EmployeeQueryDTO queryDTO, int pageSize) {
        // ???1????????????????????????
        LambdaQueryWrapper<EmployeeEntity> wrapper = buildEmployeeWrapper(queryDTO);
        if (wrapper == null) {
            // ??????????????
            Long userId = SecurityContextHolder.getUserId();
            stringRedisTemplate.opsForValue().set(
                    buildListCacheKeyWithUserInfo(queryDTO, 1, userId), EMPTY_MARKER, EMPTY_TTL_MINUTES, TimeUnit.MINUTES);
            return PageResult.of(List.of(), 0, 1, pageSize);
        }

        Page<EmployeeEntity> page1Query = new Page<>(1, pageSize + 1);
        Page<EmployeeEntity> resultPage = employeeMapper.selectPage(page1Query, wrapper);

        List<EmployeeListVO> records = resultPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        long total = resultPage.getTotal();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        boolean hasMore = records.size() > pageSize;
        if (hasMore) {
            records = records.subList(0, pageSize);
        }

        PageResult<EmployeeListVO> page1 = new PageResult<>();
        page1.setRecords(new java.util.ArrayList<>(records));
        page1.setTotal(total);
        page1.setPageNum(1);
        page1.setPageSize(pageSize);
        page1.setPages(totalPages);

        // ???1?
        Long userId = SecurityContextHolder.getUserId();
        stringRedisTemplate.opsForValue().set(
                buildListCacheKeyWithUserInfo(queryDTO, 1, userId), JSONUtil.toJsonStr(page1), CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        // ?????2-9??Redis
        if (hasMore && totalPages > 1) {
            asyncCacheEmployeePages(queryDTO, pageSize, total, totalPages);
        }

        return page1;
    }

    /**
     * ??????????2-9??Redis?
     *
     * @param queryDTO   ????
     * @param pageSize   ????
     * @param total      ????
     * @param totalPages ???
     * ??????????StringRedisTemplate(spring-data-redis), JSONUtil(hutool), CompletableFuture(JDK)
     */
    private void asyncCacheEmployeePages(EmployeeQueryDTO queryDTO, int pageSize, long total, int totalPages) {
        EmployeeQueryDTO cacheQuery = new EmployeeQueryDTO();
        cacheQuery.setKeyword(queryDTO.getKeyword());
        cacheQuery.setDeptIds(queryDTO.getDeptIds());
        cacheQuery.setEmploymentStatus(queryDTO.getEmploymentStatus());
        cacheQuery.setJobLevel(queryDTO.getJobLevel());
        cacheQuery.setHireDateStart(queryDTO.getHireDateStart());
        cacheQuery.setHireDateEnd(queryDTO.getHireDateEnd());
        cacheQuery.setPageSize(pageSize);

        // 传递当前用户的ID到异步线程
        Long currentUserId = SecurityContextHolder.getUserId();
        Long currentDeptId = SecurityContextHolder.getDeptId();
        List<Long> currentRoleIds = SecurityContextHolder.getRoleIds();

        CompletableFuture.runAsync(() -> {
            int pagesToCache = Math.min(CACHE_MAX_PAGES, totalPages);
            for (int pageNum = 2; pageNum <= pagesToCache; pageNum++) {
                // 使用传递过来的用户ID构建缓存键
                String cacheKey = buildListCacheKeyWithUserInfo(queryDTO, pageNum, currentUserId);
                // ???????
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cacheKey))) {
                    continue;
                }
                try {
                    cacheQuery.setPageNum(pageNum);
                    // 在异步线程中使用传递过来的用户信息构建包装器
                    LambdaQueryWrapper<EmployeeEntity> wrapper = buildEmployeeWrapperWithUserInfo(cacheQuery, currentUserId, currentDeptId, currentRoleIds);
                    if (wrapper == null) {
                        stringRedisTemplate.opsForValue().set(cacheKey, EMPTY_MARKER, EMPTY_TTL_MINUTES, TimeUnit.MINUTES);
                        continue;
                    }
                    Page<EmployeeEntity> page = new Page<>(pageNum, pageSize);
                    Page<EmployeeEntity> result = employeeMapper.selectPage(page, wrapper);
                    List<EmployeeListVO> pageRecords = result.getRecords().stream()
                            .map(this::convertToListVO)
                            .collect(Collectors.toList());

                    PageResult<EmployeeListVO> pageResult = new PageResult<>();
                    pageResult.setRecords(pageRecords);
                    pageResult.setTotal(total);
                    pageResult.setPageNum(pageNum);
                    pageResult.setPageSize(pageSize);
                    pageResult.setPages(totalPages);

                    if (pageRecords.isEmpty()) {
                        stringRedisTemplate.opsForValue().set(cacheKey, EMPTY_MARKER, EMPTY_TTL_MINUTES, TimeUnit.MINUTES);
                    } else {
                        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(pageResult), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                    }
                } catch (Exception e) {
                    log.error("?????????{}???", pageNum, e);
                }
            }
        }).exceptionally(ex -> {
            log.error("??????????", ex);
            return null;
        });
    }

    /**
     * 使用传递的用户信息构建员工分页查询的 QueryWrapper（含数据权限和业务过滤）
     *
     * @param queryDTO 查询参数
     * @param userId 当前用户ID
     * @param deptId 当前用户部门ID
     * @param roleIds 当前用户角色ID列表
     * @return QueryWrapper，返回 null 表示数据权限无交集，应返回空结果
     */
    private LambdaQueryWrapper<EmployeeEntity> buildEmployeeWrapperWithUserInfo(EmployeeQueryDTO queryDTO, Long userId, Long deptId, List<Long> roleIds) {
        LambdaQueryWrapper<EmployeeEntity> wrapper = Wrappers.lambdaQuery();

        // ========== 数据权限过滤 ==========
        List<Long> accessibleDeptIds = resolveAccessibleDeptIdsWithUserInfo(userId, deptId, roleIds);
        if (accessibleDeptIds != null) {
            if (accessibleDeptIds.isEmpty()) {
                wrapper.eq(EmployeeEntity::getCreateBy, userId);
            } else {
                if (queryDTO.getDeptIds() != null && !queryDTO.getDeptIds().isEmpty()) {
                    List<Long> intersected = queryDTO.getDeptIds().stream()
                            .filter(accessibleDeptIds::contains)
                            .collect(Collectors.toList());
                    if (intersected.isEmpty()) {
                        return null;
                    }
                    wrapper.in(EmployeeEntity::getDeptId, intersected);
                } else {
                    wrapper.in(EmployeeEntity::getDeptId, accessibleDeptIds);
                }
            }
        }

        // 关键词搜索（姓名/工号/手机号）
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(EmployeeEntity::getEmployeeName, queryDTO.getKeyword())
                    .or()
                    .like(EmployeeEntity::getEmployeeNo, queryDTO.getKeyword())
                    .or()
                    .like(EmployeeEntity::getPhone, queryDTO.getKeyword()));
        }

        // 部门筛选（全量权限且前端传了 deptIds 时生效）
        if (queryDTO.getDeptIds() != null && !queryDTO.getDeptIds().isEmpty()
                && accessibleDeptIds == null) {
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

        return wrapper;
    }

    /**
     * 使用传递的用户信息获取可访问的部门ID列表
     * 
     * @param userId 当前用户ID
     * @param deptId 当前用户部门ID
     * @param roleIds 当前用户角色ID列表
     * @return 可访问的部门ID列表
     */
    private List<Long> resolveAccessibleDeptIdsWithUserInfo(Long userId, Long deptId, List<Long> roleIds) {
        if (userId == null) {
            return List.of(); // 未登录，返回仅本人
        }

        // 获取用户角色列表
        List<RoleEntity> roles = roleService.getRolesByUserId(userId);
        if (roles.isEmpty()) {
            return List.of(); // 无角色，返回仅本人
        }

        // 检查是否为超级管理员角色
        boolean isAdmin = roles.stream()
                .anyMatch(r -> "ADMIN".equals(r.getRoleCode()) || "ROLE_ADMIN".equals(r.getRoleCode()));
        if (isAdmin) {
            return null; // ADMIN 全量权限
        }

        // 取最大数据权限范围（多角色时继承最宽权限）
        int dataScope = roles.stream()
                .mapToInt(r -> r.getDataScope() != null ? r.getDataScope() : 1)
                .max()
                .orElse(1);

        switch (dataScope) {
            case 4:
                // 全部
                return null;
            case 3: {
                // 本部门及下属
                Long resolvedDeptId = deptId != null ? deptId : resolveUserDeptId(userId);
                if (resolvedDeptId == null) {
                    return List.of(); // 无部门信息，降级为仅本人
                }
                return deptService.getSubDeptIds(resolvedDeptId);
            }
            case 2: {
                // 本部门
                Long resolvedDeptId = deptId != null ? deptId : resolveUserDeptId(userId);
                if (resolvedDeptId == null) {
                    return List.of(); // 无部门信息，降级为仅本人
                }
                return List.of(resolvedDeptId);
            }
            case 1:
            default:
                // 仅本人
                return List.of();
        }
    }

    private PageResult<EmployeeListVO> listEmployeesByCursor(EmployeeQueryDTO queryDTO) {
        int pageSize = Math.min(queryDTO.getPageSize(), 100);
        Long lastId = queryDTO.getLastId();

        List<Long> deptIds = queryDTO.getDeptIds();
        Long createBy = null;
        List<Long> accessibleDeptIds = resolveAccessibleDeptIds();
        if (accessibleDeptIds != null) {
            if (accessibleDeptIds.isEmpty()) {
                createBy = SecurityContextHolder.getUserId();
            } else {
                if (deptIds != null && !deptIds.isEmpty()) {
                    deptIds = deptIds.stream().filter(accessibleDeptIds::contains).collect(Collectors.toList());
                    if (deptIds.isEmpty()) {
                        return PageResult.of(List.of(), 0, 1, pageSize);
                    }
                } else {
                    deptIds = accessibleDeptIds;
                }
            }
        }

        // ????????????
        List<EmployeeEntity> entities = employeeMapper.selectPageByCursor(
                lastId, pageSize + 1, queryDTO.getKeyword(), deptIds,
                queryDTO.getEmploymentStatus(), queryDTO.getJobLevel(),
                queryDTO.getHireDateStart(), queryDTO.getHireDateEnd(), createBy);

        boolean hasMore = entities.size() > pageSize;
        if (hasMore) {
            entities = entities.subList(0, pageSize);
        }

        List<EmployeeListVO> records = entities.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        Long nextLastId = records.isEmpty() ? null : records.get(records.size() - 1).getId();

        PageResult<EmployeeListVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(hasMore ? -1 : entities.size());
        result.setPageSize(pageSize);
        result.setPages(hasMore ? -1 : 1);
        return result;
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

        // 带重试的工号生成和插入（处理并发冲突）
        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            EmployeeGenNoVO genNoVO = generateEmployeeNoWithRetry(dept.getDeptCode(), i);
            entity.setEmployeeNo(genNoVO.getEmployeeNo());
            try {
                // 保存员工记录
                employeeMapper.insert(entity);

                // 创建系统账号（独立事务，失败不回滚员工创建）
                createUserAccountInNewTransaction(entity);

                // 发布员工创建事件，更新部门人数
                eventPublisher.publishEvent(new EmployeeChangeEvent(this, EmployeeChangeEvent.ChangeType.CREATE, entity.getId(), entity.getDeptId(), entity.getDeptId()));

                return entity;
            } catch (DuplicateKeyException e) {
                if (i == maxRetries - 1) {
                    throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "生成工号失败，请重试");
                }
                log.warn("工号冲突，重试生成工号: {}", entity.getEmployeeNo());
            }
        }

        throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "生成工号失败，请重试");
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

        // 记录原部门ID（用于事件通知）——必须在更新entity.deptId之前记录
        Long oldDeptId = entity.getDeptId();

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

        // 同步更新 sys_user 的 dept_id（如果部门发生变更）
        if (updateDTO.getDeptId() != null && !updateDTO.getDeptId().equals(oldDeptId)) {
            syncUserDeptId(entity.getUserId(), updateDTO.getDeptId());
        }

        // 发布员工更新事件，更新部门人数
        if (updateDTO.getDeptId() != null && !updateDTO.getDeptId().equals(oldDeptId)) {
            eventPublisher.publishEvent(new EmployeeChangeEvent(this, EmployeeChangeEvent.ChangeType.UPDATE, id, oldDeptId, updateDTO.getDeptId()));
        }

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

        // 发布员工删除事件，更新部门人数
        eventPublisher.publishEvent(new EmployeeChangeEvent(this, EmployeeChangeEvent.ChangeType.DELETE, id, entity.getDeptId(), null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeGenNoVO generateEmployeeNo(String deptCode) {
        return generateEmployeeNoWithRetry(deptCode, 0);
    }

    /**
     * 生成工号（带重试次数参数，用于并发冲突时生成不同工号）
     */
    private EmployeeGenNoVO generateEmployeeNoWithRetry(String deptCode, int retryCount) {
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

        // 如果有重试次数，加上重试偏移量避免冲突
        nextSeq += retryCount;

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

    // ==================== 数据权限 ====================

    /**
     * 获取当前用户可访问的部门ID列表。
     * <p>
     * 返回值含义：
     * <ul>
     *   <li>{@code null} — 全量权限（ADMIN 或 data_scope=4），不限制部门</li>
     *   <li>空列表 — 仅本人权限（data_scope=1），按 create_by 过滤</li>
     *   <li>非空列表 — 可访问的部门ID集合，按 dept_id IN (...) 过滤</li>
     * </ul>
     * </p>
     */
    private List<Long> resolveAccessibleDeptIds() {
        Long userId = SecurityContextHolder.getUserId();
        if (userId == null) {
            return List.of(); // 未登录，返回仅本人
        }

        // 获取用户角色列表
        List<RoleEntity> roles = roleService.getRolesByUserId(userId);
        if (roles.isEmpty()) {
            return List.of(); // 无角色，返回仅本人
        }

        // 检查是否为超级管理员角色
        boolean isAdmin = roles.stream()
                .anyMatch(r -> "ADMIN".equals(r.getRoleCode()) || "ROLE_ADMIN".equals(r.getRoleCode()));
        if (isAdmin) {
            return null; // ADMIN 全量权限
        }

        // 取最大数据权限范围（多角色时继承最宽权限）
        int dataScope = roles.stream()
                .mapToInt(r -> r.getDataScope() != null ? r.getDataScope() : 1)
                .max()
                .orElse(1);

        switch (dataScope) {
            case 4:
                // 全部
                return null;
            case 3: {
                // 本部门及下属
                Long deptId = resolveUserDeptId(userId);
                if (deptId == null) {
                    return List.of(); // 无部门信息，降级为仅本人
                }
                return deptService.getSubDeptIds(deptId);
            }
            case 2: {
                // 本部门
                Long deptId = resolveUserDeptId(userId);
                if (deptId == null) {
                    return List.of(); // 无部门信息，降级为仅本人
                }
                return List.of(deptId);
            }
            case 1:
            default:
                // 仅本人
                return List.of();
        }
    }

    /**
     * 获取用户对应的部门ID。
     * <p>
     * 优先从 SecurityContextHolder 中获取（JWT token 中的 deptId），
     * 如果为空则通过员工档案反查（sys_user.dept_id 可能未同步，通过 employee.user_id 查找）。
     * </p>
     *
     * @param userId 用户ID
     * @return 部门ID，可能为 null
     */
    private Long resolveUserDeptId(Long userId) {
        // 1. 优先从 JWT token 中获取（已同步的情况）
        Long deptId = SecurityContextHolder.getDeptId();
        if (deptId != null) {
            return deptId;
        }

        // 2. 降级：从员工档案中反查（sys_user.dept_id 未同步的情况）
        try {
            EmployeeEntity employee = employeeMapper.selectOne(
                    Wrappers.<EmployeeEntity>lambdaQuery()
                            .eq(EmployeeEntity::getUserId, userId)
            );
            if (employee != null && employee.getDeptId() != null) {
                log.debug("通过员工档案反查部门ID: userId={}, deptId={}", userId, employee.getDeptId());
                return employee.getDeptId();
            }
        } catch (Exception e) {
            log.warn("通过员工档案反查部门ID失败: userId={}", userId, e);
        }

        return null;
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
     * 在独立事务中创建系统账号。
     * <p>
     * 使用 REQUIRES_NEW 确保用户创建失败（如 auth 模块的 @TableLogic 导致
     * 唯一约束冲突）不会将外层员工创建事务标记为 rollback-only。
     * </p>
     *
     * @param entity 已保存的员工实体
     */
    private void createUserAccountInNewTransaction(EmployeeEntity entity) {
        try {
            Long userId = transactionTemplate.execute(status -> {
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
                userCreateDTO.setDeptId(entity.getDeptId());             // 同步部门ID

                // 调用用户服务创建账号
                UserCreateResultVO result = userService.createUser(userCreateDTO);

                log.info("员工 [{}] 系统账号创建成功，userId={}", entity.getEmployeeName(), result.getId());
                return result.getId();
            });

            // 回填 user_id 到员工记录（在主事务中）
            if (userId != null) {
                entity.setUserId(userId);
                employeeMapper.updateById(entity);
            }
        } catch (Exception e) {
            log.error("创建员工系统账号失败，employeeId={}", entity.getId(), e);
            // 不阻断主流程：员工已创建成功，账号可后续补建
        }
    }

    /**
     * 生成初始密码
     *
     * @return 初始密码
     */
    private String generateRandomPassword() {
        return "123456";
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

        // 清理已逻辑删除的同手机号记录，防止插入时触发数据库 uk_hr_employee_phone 唯一约束
        // MyBatis-Plus @TableLogic 过滤了 is_deleted=1 的记录，但数据库唯一约束不考虑逻辑删除状态
        // 将已删除记录的手机号改为 "deleted_<id>_<timestamp>" 释放原手机号
        String newPhone = "deleted_" + System.currentTimeMillis();
        int updatedRows = employeeMapper.releasePhoneForDeleted(phone, newPhone);
        if (updatedRows > 0) {
            log.info("已释放逻辑删除记录的手机号 {} → {}，共 {} 条", phone, newPhone, updatedRows);
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
     * 同步更新用户表的 dept_id
     * <p>
     * 当员工部门发生变更时，同步更新关联的系统账号的部门ID
     * </p>
     *
     * @param userId 用户ID
     * @param deptId 新部门ID
     */
    private void syncUserDeptId(Long userId, Long deptId) {
        if (userId == null) {
            return;
        }
        try {
            userService.updateUserDept(userId, deptId);
            log.info("同步更新用户部门成功，userId={}, deptId={}", userId, deptId);
        } catch (Exception e) {
            log.error("同步更新用户部门失败，userId={}", userId, e);
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

    /**
     * ???????? Key
     *
     * @param queryDTO ????
     * @param pageNum  ??
     * @return ?? Key
     * ??????????SecurityContextHolder(hrms-common)
     */
    private String buildListCacheKey(EmployeeQueryDTO queryDTO, int pageNum) {
        Long userId = SecurityContextHolder.getUserId();
        StringBuilder sb = new StringBuilder(CACHE_PREFIX);
        sb.append(userId).append("|");
        sb.append(queryDTO.getKeyword() != null ? queryDTO.getKeyword() : "").append("|");
        sb.append(queryDTO.getDeptIds() != null ? queryDTO.getDeptIds().toString() : "").append("|");
        sb.append(queryDTO.getEmploymentStatus() != null ? queryDTO.getEmploymentStatus().toString() : "").append("|");
        sb.append(queryDTO.getJobLevel() != null ? queryDTO.getJobLevel() : "").append("|");
        sb.append(queryDTO.getHireDateStart() != null ? queryDTO.getHireDateStart().toString() : "").append("|");
        sb.append(queryDTO.getHireDateEnd() != null ? queryDTO.getHireDateEnd().toString() : "").append("|");
        sb.append(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 20).append("|");
        sb.append(pageNum);
        return sb.toString();
    }

    /**
     * ???????? Key
     *
     * @param queryDTO ????
     * @param pageNum  ??
     * @param userId   ????ID
     * @return ?? Key
     * ??????????SecurityContextHolder(hrms-common)
     */
    private String buildListCacheKeyWithUserInfo(EmployeeQueryDTO queryDTO, int pageNum, Long userId) {
        StringBuilder sb = new StringBuilder(CACHE_PREFIX);
        sb.append(userId != null ? userId : "anonymous").append("|");
        sb.append(queryDTO.getKeyword() != null ? queryDTO.getKeyword() : "").append("|");
        sb.append(queryDTO.getDeptIds() != null ? queryDTO.getDeptIds().toString() : "").append("|");
        sb.append(queryDTO.getEmploymentStatus() != null ? queryDTO.getEmploymentStatus().toString() : "").append("|");
        sb.append(queryDTO.getJobLevel() != null ? queryDTO.getJobLevel() : "").append("|");
        sb.append(queryDTO.getHireDateStart() != null ? queryDTO.getHireDateStart().toString() : "").append("|");
        sb.append(queryDTO.getHireDateEnd() != null ? queryDTO.getHireDateEnd().toString() : "").append("|");
        sb.append(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 20).append("|");
        sb.append(pageNum);
        return sb.toString();
    }

    /**
     * 审批通过后同步员工档案字段。
     *
     * @param employeeId 员工ID
     * @param updateDTO 审批联动更新参数
     * @return 更新后的员工实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeeEntity syncEmployeeForApproval(Long employeeId, EmployeeApprovalSyncUpdateDTO updateDTO) {
        EmployeeEntity entity = employeeMapper.selectById(employeeId);
        if (entity == null) {
            throw new GlobalException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
        if (updateDTO == null) {
            return entity;
        }

        Long oldDeptId = entity.getDeptId();
        Long newDeptId = updateDTO.getDeptId();

        // 审批联动前先校验基础组织信息，避免写入不存在的部门或职位。
        if (newDeptId != null && !Objects.equals(newDeptId, entity.getDeptId())) {
            validateDeptExists(newDeptId);
        }
        if (updateDTO.getPostId() != null && !Objects.equals(updateDTO.getPostId(), entity.getPostId())) {
            validatePostExists(updateDTO.getPostId());
        }

        // 仅更新审批联动涉及的字段，避免覆盖员工档案其他业务字段。
        LambdaUpdateWrapper<EmployeeEntity> updateWrapper = Wrappers.lambdaUpdate(EmployeeEntity.class)
                .eq(EmployeeEntity::getId, employeeId);
        boolean hasUpdate = false;

        if (updateDTO.getEmploymentStatus() != null
                && !Objects.equals(updateDTO.getEmploymentStatus(), entity.getEmploymentStatus())) {
            updateWrapper.set(EmployeeEntity::getEmploymentStatus, updateDTO.getEmploymentStatus());
            entity.setEmploymentStatus(updateDTO.getEmploymentStatus());
            hasUpdate = true;
        }
        if (newDeptId != null && !Objects.equals(newDeptId, entity.getDeptId())) {
            updateWrapper.set(EmployeeEntity::getDeptId, newDeptId);
            entity.setDeptId(newDeptId);
            hasUpdate = true;
        }
        if (updateDTO.getPostId() != null && !Objects.equals(updateDTO.getPostId(), entity.getPostId())) {
            updateWrapper.set(EmployeeEntity::getPostId, updateDTO.getPostId());
            entity.setPostId(updateDTO.getPostId());
            hasUpdate = true;
        }
        if (updateDTO.getJobLevel() != null && !Objects.equals(updateDTO.getJobLevel(), entity.getJobLevel())) {
            updateWrapper.set(EmployeeEntity::getJobLevel, updateDTO.getJobLevel());
            entity.setJobLevel(updateDTO.getJobLevel());
            hasUpdate = true;
        }
        if (updateDTO.getLeaderId() != null && !Objects.equals(updateDTO.getLeaderId(), entity.getLeaderId())) {
            updateWrapper.set(EmployeeEntity::getLeaderId, updateDTO.getLeaderId());
            entity.setLeaderId(updateDTO.getLeaderId());
            hasUpdate = true;
        }

        if (!hasUpdate) {
            return entity;
        }

        employeeMapper.update(null, updateWrapper);

        // 部门变更后同步用户归属部门，保证账号侧组织信息与员工档案一致。
        if (newDeptId != null && !Objects.equals(newDeptId, oldDeptId)) {
            syncUserDeptId(entity.getUserId(), newDeptId);
            // 发布员工变更事件，触发部门人数刷新等现有联动逻辑。
            eventPublisher.publishEvent(new EmployeeChangeEvent(
                    this,
                    EmployeeChangeEvent.ChangeType.UPDATE,
                    employeeId,
                    oldDeptId,
                    newDeptId
            ));
        }

        return entity;
    }
}
