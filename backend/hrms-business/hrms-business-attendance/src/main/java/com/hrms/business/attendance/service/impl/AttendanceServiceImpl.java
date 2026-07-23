package com.hrms.business.attendance.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.dto.ApprovalDetailVO;
import com.hrms.business.approval.service.ApprovalTaskService;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.attendance.cache.AttendanceCacheKeys;
import com.hrms.business.attendance.convert.AttendanceCalendarSummaryEnricher;
import com.hrms.business.attendance.convert.AttendanceGroupConvert;
import com.hrms.business.attendance.dto.AttendanceClockRequestDTO;
import com.hrms.business.attendance.dto.AttendanceCorrectionCreateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupMemberRangeDTO;
import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.dto.AttendanceGroupRecordQueryDTO;
import com.hrms.business.attendance.dto.AttendanceLeaveManageQueryDTO;
import com.hrms.business.attendance.dto.LeaveCreateRequestDTO;
import com.hrms.business.attendance.dto.MonthlyStatGenerateRequestDTO;
import com.hrms.business.attendance.entity.AttendanceCorrectionEntity;
import com.hrms.business.attendance.entity.AttendanceGroupEntity;
import com.hrms.business.attendance.entity.AttendanceGroupMemberEntity;
import com.hrms.business.attendance.entity.AttendanceRecordEntity;
import com.hrms.business.attendance.entity.EmployeeSnapshotEntity;
import com.hrms.business.attendance.entity.LeaveBalanceEntity;
import com.hrms.business.attendance.entity.LeaveRequestEntity;
import com.hrms.business.attendance.entity.DictDataEntity;
import com.hrms.business.attendance.common.enums.ClockPeriodEnum;
import com.hrms.business.attendance.mapper.AttendanceGroupMapper;
import com.hrms.business.attendance.mapper.AttendanceCorrectionMapper;
import com.hrms.business.attendance.mapper.AttendanceGroupMemberMapper;
import com.hrms.business.attendance.mapper.AttendanceRecordMapper;
import com.hrms.business.attendance.mapper.AttendanceEmployeeSnapshotMapper;
import com.hrms.business.attendance.mapper.LeaveBalanceMapper;
import com.hrms.business.attendance.mapper.LeaveRequestMapper;
import com.hrms.business.attendance.mapper.AttendanceDictDataMapper;
import com.hrms.business.attendance.mq.producer.AttendanceClockCreatedProducer;
import com.hrms.business.attendance.mq.event.AttendanceClockCreatedEvent;
import com.hrms.business.attendance.mq.handler.AttendanceClockEventHandler;
import com.hrms.business.attendance.mq.event.AttendanceMonthlyStatGenerateMessage;
import com.hrms.business.attendance.mq.producer.AttendanceMonthlyStatGenerateProducer;
import com.hrms.business.attendance.service.AttendanceCalendarConfigService;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceClockVO;
import com.hrms.business.attendance.vo.AttendanceCalendarDayVO;
import com.hrms.business.attendance.vo.AttendanceCalendarVO;
import com.hrms.business.attendance.vo.AttendanceCorrectionCreateVO;
import com.hrms.business.attendance.vo.AttendanceGroupRecordPageVO;
import com.hrms.business.attendance.vo.AttendanceLeaveManageItemVO;
import com.hrms.business.attendance.vo.AttendanceDeptDistributionVO;
import com.hrms.business.attendance.vo.AttendanceEmployeeRankingVO;
import com.hrms.business.attendance.vo.AttendanceExceptionPieVO;
import com.hrms.business.attendance.vo.LeaveTypeVO;
import com.hrms.business.attendance.vo.LeaveBalanceVO;
import com.hrms.business.attendance.vo.LeaveCreateVO;
import com.hrms.business.attendance.vo.MonthlyStatGenerateVO;
import com.hrms.business.attendance.vo.AttendancePayrollSourceVO;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.business.attendance.vo.AttendanceSummaryDashboardVO;
import com.hrms.business.attendance.vo.AttendanceTrendPointVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.DeptDetailVO;
import com.hrms.system.organization.vo.PostVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.math.RoundingMode;

import static com.hrms.business.attendance.common.constant.AttendanceServiceConstant.*;
import static com.hrms.business.attendance.common.enums.AttendanceServiceErrorEnum.*;

/**
 * 考勤管理服务实现。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    // 考勤日历配置服务
    private final AttendanceCalendarConfigService attendanceCalendarConfigService;
    // 考勤组Mapper
    private final AttendanceGroupMapper attendanceGroupMapper;
    // 考勤组成员Mapper
    private final AttendanceGroupMemberMapper attendanceGroupMemberMapper;

    private final AttendanceRecordMapper attendanceRecordMapper;

    private final AttendanceCorrectionMapper attendanceCorrectionMapper;

    private final AttendanceEmployeeSnapshotMapper employeeSnapshotMapper;

    private final LeaveBalanceMapper leaveBalanceMapper;

    private final LeaveRequestMapper leaveRequestMapper;

    private final AttendanceDictDataMapper dictDataMapper;

    private final StringRedisTemplate stringRedisTemplate;
    // 打卡成功生产者
    private final AttendanceClockCreatedProducer attendanceClockCreatedProducer;
    //月度考勤生产者
    private final AttendanceMonthlyStatGenerateProducer attendanceMonthlyStatGenerateProducer;

    private final ApprovalEngine approvalEngine;
    // 审批任务
    private final ApprovalTaskService approvalTaskService;

    private final DeptService deptService;

    private final PostService postService;

    private final RoleService roleService;

    /**
     * 分页查询考勤组。
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<AttendanceGroupPageVO> pageAttendanceGroups(AttendanceGroupQueryDTO queryDTO) {
        String cacheKey = AttendanceCacheKeys.attendanceGroupPage(buildAttendanceGroupPageCacheKey(queryDTO));
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(cached)) {
                return JSONUtil.toBean(cached, PageResult.class);
            }
        } catch (Exception ex) {
            log.warn("load attendance group page cache failed, cacheKey={}", cacheKey, ex);
        }

        Page<AttendanceGroupEntity> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<AttendanceGroupEntity> wrapper = new LambdaQueryWrapper<AttendanceGroupEntity>()
                .like(StrUtil.isNotBlank(queryDTO.getGroupName()), AttendanceGroupEntity::getGroupName, queryDTO.getGroupName())
                .eq(queryDTO.getStatus() != null, AttendanceGroupEntity::getStatus, queryDTO.getStatus())
                .orderByDesc(AttendanceGroupEntity::getCreateTime)
                .orderByDesc(AttendanceGroupEntity::getId);
        Page<AttendanceGroupEntity> resultPage = attendanceGroupMapper.selectPage(page, wrapper);
        List<AttendanceGroupPageVO> records = resultPage.getRecords().stream()
                .map(AttendanceGroupConvert::toPageVO)
                .map(this::fillAttendanceGroupScope)
                .toList();
        PageResult<AttendanceGroupPageVO> result = PageResult.of(
                records,
                resultPage.getTotal(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );

        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), Duration.ofDays(2));
        } catch (Exception ex) {
            log.warn("save attendance group page cache failed, cacheKey={}", cacheKey, ex);
        }
        return result;
    }

    /**
     * 创建考勤组。
     * @param requestDTO 创建参数
     * @return 创建后的考勤组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceGroupPageVO createAttendanceGroup(AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        validateCreateMemberRange(requestDTO.getMemberRange());
        AttendanceGroupEntity entity = AttendanceGroupConvert.toEntity(requestDTO);
        fillGroupScope(entity, requestDTO.getMemberRange());
        attendanceGroupMapper.insert(entity);
        rebuildGroupMembers(entity.getId(), requestDTO.getMemberRange());
        evictGroupRuleCache(entity.getId());
        evictAttendanceGroupPageCache();
        return fillAttendanceGroupScope(AttendanceGroupConvert.toPageVO(entity));

    }

    /**
     * 更新考勤组。
     * @param id 考勤组ID
     * @param requestDTO 更新参数
      * @return 更新后的考勤组
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceGroupPageVO updateAttendanceGroup(Long id, AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        AttendanceGroupEntity entity = getRequiredAttendanceGroup(id);
        String previousScopeType = entity.getScopeType();
        String previousScopeValue = entity.getScopeValue();
        AttendanceGroupConvert.fillEntity(entity, requestDTO);
        fillGroupScope(entity, requestDTO.getMemberRange());
        attendanceGroupMapper.updateById(entity);
        if (isGroupScopeChanged(previousScopeType, previousScopeValue, entity.getScopeType(), entity.getScopeValue())) {
            rebuildGroupMembers(id, requestDTO.getMemberRange());
        }
        evictGroupRuleCache(id);
        evictAttendanceGroupPageCache();
        return fillAttendanceGroupScope(AttendanceGroupConvert.toPageVO(entity));
    }


    /**
     * 逻辑删除考勤组。
     *
     * @param id 考勤组ID
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),GlobalException(hrms-common)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttendanceGroup(Long id) {
        AttendanceGroupEntity entity = getRequiredAttendanceGroup(id);
        long memberCount = attendanceGroupMemberMapper.selectCount(new LambdaQueryWrapper<AttendanceGroupMemberEntity>()
                .eq(AttendanceGroupMemberEntity::getGroupId, id)
                .eq(AttendanceGroupMemberEntity::getIsDeleted, 0));
        if (memberCount > 0) {
            throw new GlobalException(ATTENDANCE_GROUP_MEMBER_EXISTS);
        }
        if (attendanceRecordMapper.countByGroupId(id) > 0) {
            throw new GlobalException(ATTENDANCE_GROUP_RECORD_EXISTS);
        }
        attendanceGroupMapper.deleteById(entity.getId());
        evictGroupRuleCache(id);
        evictAttendanceGroupPageCache();
    }

    /**
     * 分页查询考勤组打卡记录。
     *
     * @param groupId  考勤组ID
     * @param queryDTO 查询参数
     * @return 考勤组打卡记录分页结果
     * 本方法使用的工具类: PageResult(hrms-common),ChronoUnit(JDK),StrUtil(hutool)
     */
    @Override
    public PageResult<AttendanceGroupRecordPageVO> pageGroupAttendanceRecords(Long groupId,
                                                                              AttendanceGroupRecordQueryDTO queryDTO) {
        getRequiredAttendanceGroup(groupId);
        AttendanceGroupRecordQueryDTO safeQuery = queryDTO == null ? new AttendanceGroupRecordQueryDTO() : queryDTO;
        AttendanceDateRange dateRange = resolveGroupRecordDateRange(safeQuery);
        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        String cacheKey = AttendanceCacheKeys.attendanceGroupRecordPage(
                buildAttendanceGroupRecordPageCacheKey(groupId, safeQuery, dateRange, pageNum, pageSize)
        );

        if (Boolean.TRUE.equals(safeQuery.getRefreshCache())) {
            deleteCacheKey(cacheKey, "delete attendance group record page cache failed, cacheKey={}");
        } else {
            PageResult<AttendanceGroupRecordPageVO> cachedResult = loadCacheValue(
                    cacheKey,
                    new TypeReference<PageResult<AttendanceGroupRecordPageVO>>() {
                    },
                    "load attendance group record page cache failed, cacheKey={}"
            );
            if (cachedResult != null) {
                return cachedResult;
            }
        }

        List<AttendanceGroupMemberEntity> members = listEffectiveGroupMembers(groupId, dateRange.startDate(), dateRange.endDate());
        if (members.isEmpty()) {
            PageResult<AttendanceGroupRecordPageVO> emptyResult = PageResult.of(List.of(), 0, pageNum, pageSize);
            saveCacheValue(cacheKey, emptyResult, "save attendance group record page cache failed, cacheKey={}");
            return emptyResult;
        }
        List<Long> employeeIds = members.stream()
                .map(AttendanceGroupMemberEntity::getEmployeeId)
                .distinct()
                .toList();
        Map<Long, EmployeeSnapshotEntity> employeeMap = employeeSnapshotMapper.selectList(new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                        .in(EmployeeSnapshotEntity::getId, employeeIds))
                .stream()
                .collect(Collectors.toMap(EmployeeSnapshotEntity::getId, employee -> employee, (a, b) -> a));
        if (employeeMap.isEmpty()) {
            PageResult<AttendanceGroupRecordPageVO> emptyResult = PageResult.of(List.of(), 0, pageNum, pageSize);
            saveCacheValue(cacheKey, emptyResult, "save attendance group record page cache failed, cacheKey={}");
            return emptyResult;
        }
        Map<String, AttendanceRecordEntity> recordMap = attendanceRecordMapper
                .selectByGroupAndEmployeesAndDateRange(groupId, employeeIds, dateRange.startDate(), dateRange.endDate())
                .stream()
                .collect(Collectors.toMap(record -> buildRecordKey(record.getEmployeeId(), record.getRecordDate()),
                        record -> record, (a, b) -> a));
        Map<Long, String> deptNameCache = new HashMap<>();
        List<AttendanceGroupRecordPageVO> allRecords = buildGroupRecordRows(
                members, employeeMap, recordMap, deptNameCache, dateRange, safeQuery);
        allRecords.sort(Comparator.comparing(AttendanceGroupRecordPageVO::getRecordDate).reversed()
                .thenComparing(AttendanceGroupRecordPageVO::getEmployeeId));
        int fromIndex = Math.min((pageNum - 1) * pageSize, allRecords.size());
        int toIndex = Math.min(fromIndex + pageSize, allRecords.size());
        PageResult<AttendanceGroupRecordPageVO> result = PageResult.of(
                allRecords.subList(fromIndex, toIndex),
                allRecords.size(),
                pageNum,
                pageSize
        );
        saveCacheValue(cacheKey, result, "save attendance group record page cache failed, cacheKey={}");
        return result;
    }

    /**
     * 解析考勤组记录查询日期范围。
     *
     * @param queryDTO 查询参数
     * @return 日期范围
     * 本方法使用的工具类: YearMonth(JDK),ChronoUnit(JDK),GlobalException(hrms-common)
     */
    private AttendanceDateRange resolveGroupRecordDateRange(AttendanceGroupRecordQueryDTO queryDTO) {
        YearMonth yearMonth = YearMonth.now();
        if (queryDTO != null && StrUtil.isNotBlank(queryDTO.getYearMonth())) {
            try {
                yearMonth = YearMonth.parse(queryDTO.getYearMonth());
            } catch (DateTimeParseException ex) {
                throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "yearMonth格式必须为yyyy-MM");
            }
        }
        LocalDate startDate = queryDTO != null && queryDTO.getDateStart() != null
                ? queryDTO.getDateStart()
                : yearMonth.atDay(1);
        LocalDate endDate = queryDTO != null && queryDTO.getDateEnd() != null
                ? queryDTO.getDateEnd()
                : yearMonth.atEndOfMonth();
        if (endDate.isBefore(startDate)) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "dateEnd不能早于dateStart");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > GROUP_RECORD_MAX_DAYS) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "考勤组打卡记录查询范围不能超过31天");
        }
        return new AttendanceDateRange(startDate, endDate);
    }

    /**
     * 查询指定日期范围内有效的考勤组成员。
     *
     * @param groupId   考勤组ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 成员关系列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus)
     */
    private List<AttendanceGroupMemberEntity> listEffectiveGroupMembers(Long groupId, LocalDate startDate, LocalDate endDate) {
        return attendanceGroupMemberMapper.selectList(new LambdaQueryWrapper<AttendanceGroupMemberEntity>()
                .eq(AttendanceGroupMemberEntity::getGroupId, groupId)
                .eq(AttendanceGroupMemberEntity::getStatus, 1)
                .le(AttendanceGroupMemberEntity::getEffectiveStartDate, endDate)
                .and(wrapper -> wrapper.isNull(AttendanceGroupMemberEntity::getEffectiveEndDate)
                        .or()
                        .ge(AttendanceGroupMemberEntity::getEffectiveEndDate, startDate))
                .orderByAsc(AttendanceGroupMemberEntity::getEmployeeId)
                .orderByDesc(AttendanceGroupMemberEntity::getEffectiveStartDate));
    }

    /**
     * 构建考勤组打卡记录行。
     *
     * @param members       成员关系
     * @param employeeMap   员工快照
     * @param recordMap     打卡记录
     * @param deptNameCache 部门名称缓存
     * @param dateRange     日期范围
     * @param queryDTO      查询参数
     * @return 打卡记录行
     * 本方法使用的工具类: IntStream(JDK),StrUtil(hutool)
     */
    private List<AttendanceGroupRecordPageVO> buildGroupRecordRows(List<AttendanceGroupMemberEntity> members,
                                                                   Map<Long, EmployeeSnapshotEntity> employeeMap,
                                                                   Map<String, AttendanceRecordEntity> recordMap,
                                                                   Map<Long, String> deptNameCache,
                                                                   AttendanceDateRange dateRange,
                                                                   AttendanceGroupRecordQueryDTO queryDTO) {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = IntStream.rangeClosed(0, (int) ChronoUnit.DAYS.between(dateRange.startDate(), dateRange.endDate()))
                .mapToObj(dateRange.startDate()::plusDays)
                .toList();
        List<AttendanceGroupRecordPageVO> rows = new ArrayList<>();
        List<Long> employeeIds = members.stream()
                .map(AttendanceGroupMemberEntity::getEmployeeId)
                .distinct()
                .toList();
        for (Long employeeId : employeeIds) {
            EmployeeSnapshotEntity employee = employeeMap.get(employeeId);
            if (employee == null || !matchesGroupRecordEmployeeFilter(employee, queryDTO)) {
                continue;
            }
            for (LocalDate date : dates) {
                if (date.isAfter(today)) {
                    continue;
                }
                if (!isMemberEffectiveOnDate(members, employeeId, date)) {
                    continue;
                }
                AttendanceRecordEntity record = recordMap.get(buildRecordKey(employeeId, date));
                AttendanceStatusView statusView = resolveGroupRecordStatus(record);
                if (queryDTO != null && StrUtil.isNotBlank(queryDTO.getStatus())
                        && !queryDTO.getStatus().equalsIgnoreCase(statusView.status())) {
                    continue;
                }
                rows.add(buildGroupRecordPageVO(employee, record, date, statusView, deptNameCache));
            }
        }
        return rows;
    }

    /**
     * 判断员工是否匹配考勤组记录查询过滤条件。
     *
     * @param employee 员工快照
     * @param queryDTO 查询参数
     * @return 是否匹配
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private boolean matchesGroupRecordEmployeeFilter(EmployeeSnapshotEntity employee, AttendanceGroupRecordQueryDTO queryDTO) {
        if (queryDTO == null) {
            return true;
        }
        if (queryDTO.getDepartmentId() != null && !queryDTO.getDepartmentId().equals(employee.getDeptId())) {
            return false;
        }
        if (StrUtil.isBlank(queryDTO.getKeyword())) {
            return true;
        }
        String keyword = queryDTO.getKeyword().trim();
        return StrUtil.contains(employee.getEmployeeName(), keyword)
                || StrUtil.contains(employee.getEmployeeNo(), keyword);
    }

    /**
     * 判断员工在指定日期是否为考勤组有效成员。
     *
     * @param members    成员关系列表
     * @param employeeId 员工ID
     * @param date       日期
     * @return 是否有效
     * 本方法使用的工具类: 无
     */
    private boolean isMemberEffectiveOnDate(List<AttendanceGroupMemberEntity> members, Long employeeId, LocalDate date) {
        return members.stream().anyMatch(member -> employeeId.equals(member.getEmployeeId())
                && !member.getEffectiveStartDate().isAfter(date)
                && (member.getEffectiveEndDate() == null || !member.getEffectiveEndDate().isBefore(date)));
    }

    /**
     * 构建考勤组打卡记录分页行。
     *
     * @param employee      员工快照
     * @param record        打卡记录
     * @param recordDate    打卡日期
     * @param statusView    综合状态
     * @param deptNameCache 部门名称缓存
     * @return 打卡记录分页行
     * 本方法使用的工具类: AttendanceGroupRecordPageVO(本模块vo包)
     */
    private AttendanceGroupRecordPageVO buildGroupRecordPageVO(EmployeeSnapshotEntity employee,
                                                               AttendanceRecordEntity record,
                                                               LocalDate recordDate,
                                                               AttendanceStatusView statusView,
                                                               Map<Long, String> deptNameCache) {
        return AttendanceGroupRecordPageVO.builder()
                .recordId(record == null ? null : record.getId())
                .recordDate(recordDate)
                .employeeId(employee.getId())
                .employeeName(employee.getEmployeeName())
                .employeeNo(employee.getEmployeeNo())
                .deptId(employee.getDeptId())
                .deptName(resolveDeptName(employee.getDeptId(), deptNameCache))
                .clockInTime(record == null || record.getClockInTime() == null ? null : record.getClockInTime().toLocalTime())
                .clockOutTime(record == null || record.getClockOutTime() == null ? null : record.getClockOutTime().toLocalTime())
                .clockInStatus(record == null ? null : record.getClockInStatus())
                .clockOutStatus(record == null ? null : record.getClockOutStatus())
                .status(statusView.status())
                .statusName(statusView.statusName())
                .build();
    }

    /**
     * 解析部门名称。
     *
     * @param deptId        部门ID
     * @param deptNameCache 部门名称缓存
     * @return 部门名称
     * 本方法使用的工具类: DeptService(hrms-system-organization)
     */
    private String resolveDeptName(Long deptId, Map<Long, String> deptNameCache) {
        if (deptId == null) {
            return null;
        }
        if (deptNameCache.containsKey(deptId)) {
            return deptNameCache.get(deptId);
        }
        try {
            DeptDetailVO dept = deptService.getDeptById(deptId);
            String deptName = dept == null ? null : dept.getDeptName();
            deptNameCache.put(deptId, deptName);
            return deptName;
        } catch (Exception ex) {
            log.warn("resolve attendance record dept name failed, deptId={}", deptId, ex);
            deptNameCache.put(deptId, null);
            return null;
        }
    }

    /**
     * 合成考勤组打卡记录综合状态。
     *
     * @param record 打卡记录
     * @return 综合状态
     * 本方法使用的工具类: 无
     */
    private AttendanceStatusView resolveGroupRecordStatus(AttendanceRecordEntity record) {
        if (record == null || (record.getClockInTime() == null && record.getClockOutTime() == null)) {
            return new AttendanceStatusView("ABSENCE", "缺勤");
        }
        if (record.getClockInTime() == null) {
            return new AttendanceStatusView("CLOCK_IN_MISSING", "上班缺卡");
        }
        if (record.getClockOutTime() == null) {
            return new AttendanceStatusView("CLOCK_OUT_MISSING", "下班缺卡");
        }
        boolean late = "LATE".equals(record.getClockInStatus());
        boolean earlyLeave = "EARLY_LEAVE".equals(record.getClockOutStatus());
        if (late && earlyLeave) {
            return new AttendanceStatusView("ABNORMAL", "异常");
        }
        if (late) {
            return new AttendanceStatusView("LATE", "迟到");
        }
        if (earlyLeave) {
            return new AttendanceStatusView("EARLY_LEAVE", "早退");
        }
        return new AttendanceStatusView("NORMAL", "正常");
    }

    /**
     * 构建打卡记录 Map Key。
     *
     * @param employeeId 员工ID
     * @param recordDate 打卡日期
     * @return Map Key
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private String buildRecordKey(Long employeeId, LocalDate recordDate) {
        return StrUtil.format("{}:{}", employeeId, recordDate);
    }

    /**
     * 解析考勤统计月份。
     *
     * @param yearMonth 月份
     * @return 年月
     * 本方法使用的工具类: YearMonth(JDK),GlobalException(hrms-common)
     */
    private YearMonth parseDashboardYearMonth(String yearMonth) {
        if (StrUtil.isBlank(yearMonth)) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "yearMonth不能为空");
        }
        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException ex) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "yearMonth格式必须为yyyy-MM");
        }
    }

    /**
     * 解析考勤统计数据权限范围。
     *
     * @param requestedDeptId 请求部门ID
     * @return 统计范围
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),RoleService(hrms-system-auth),DeptService(hrms-system-organization)
     */
    private AttendanceDashboardScope resolveDashboardScope(Long requestedDeptId) {
        Long userId = SecurityContextHolder.getUserId();
        List<RoleEntity> roles = roleService.getRolesByUserId(userId);
        boolean fullScope = roles.stream().anyMatch(this::isDashboardFullScopeRole);
        if (fullScope) {
            return new AttendanceDashboardScope(requestedDeptId == null ? null : resolveDeptAndChildren(requestedDeptId));
        }
        Long currentDeptId = SecurityContextHolder.getDeptId();
        if (currentDeptId == null) {
            currentDeptId = getCurrentEmployeeSnapshot().getDeptId();
        }
        if (currentDeptId == null) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "当前用户未配置部门，无法查看考勤统计");
        }
        int dataScope = roles.stream()
                .map(RoleEntity::getDataScope)
                .filter(scope -> scope != null)
                .max(Integer::compareTo)
                .orElse(2);
        Set<Long> accessibleDeptIds = dataScope == 3
                ? resolveDeptAndChildren(currentDeptId)
                : Set.of(currentDeptId);
        if (requestedDeptId == null) {
            return new AttendanceDashboardScope(accessibleDeptIds);
        }
        if (!accessibleDeptIds.contains(requestedDeptId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "无权查看该部门考勤统计");
        }
        Set<Long> requestedDeptIds = resolveDeptAndChildren(requestedDeptId);
        requestedDeptIds.retainAll(accessibleDeptIds);
        return new AttendanceDashboardScope(requestedDeptIds);
    }

    /**
     * 判断角色是否拥有考勤统计全量权限。
     *
     * @param role 角色
     * @return 是否拥有全量权限
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private boolean isDashboardFullScopeRole(RoleEntity role) {
        if (role == null) {
            return false;
        }
        if (Integer.valueOf(4).equals(role.getDataScope())) {
            return true;
        }
        return StrUtil.isNotBlank(role.getRoleCode())
                && DASHBOARD_FULL_SCOPE_ROLE_CODES.contains(role.getRoleCode().toUpperCase());
    }

    /**
     * 查询部门及下级部门ID。
     *
     * @param deptId 部门ID
     * @return 部门ID集合
     * 本方法使用的工具类: DeptService(hrms-system-organization),HashSet(JDK)
     */
    private Set<Long> resolveDeptAndChildren(Long deptId) {
        if (deptId == null) {
            return Set.of();
        }
        try {
            return new HashSet<>(deptService.getSubDeptIds(deptId));
        } catch (Exception ex) {
            log.warn("resolve dashboard dept scope failed, deptId={}", deptId, ex);
            return new HashSet<>(Set.of(deptId));
        }
    }

    /**
     * 查询考勤统计员工范围。
     *
     * @param targetDeptIds 目标部门ID集合
     * @return 员工快照列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),Collections(JDK)
     */
    private List<EmployeeSnapshotEntity> listDashboardEmployees(Set<Long> targetDeptIds) {
        if (targetDeptIds != null && targetDeptIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<EmployeeSnapshotEntity> wrapper = new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                .ne(EmployeeSnapshotEntity::getEmploymentStatus, 4);
        if (targetDeptIds != null) {
            wrapper.in(EmployeeSnapshotEntity::getDeptId, targetDeptIds);
        }
        List<EmployeeSnapshotEntity> employees = employeeSnapshotMapper.selectList(wrapper);
        return employees == null ? Collections.emptyList() : employees;
    }

    /**
     * 查询考勤统计有效成员关系。
     *
     * @param employeeIds 员工ID列表
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 成员关系列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),Collections(JDK)
     */
    private List<AttendanceGroupMemberEntity> listDashboardMembers(List<Long> employeeIds, LocalDate startDate, LocalDate endDate) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return List.of();
        }
        List<AttendanceGroupMemberEntity> members = attendanceGroupMemberMapper.selectList(new LambdaQueryWrapper<AttendanceGroupMemberEntity>()
                .in(AttendanceGroupMemberEntity::getEmployeeId, employeeIds)
                .eq(AttendanceGroupMemberEntity::getStatus, 1)
                .le(AttendanceGroupMemberEntity::getEffectiveStartDate, endDate)
                .and(wrapper -> wrapper.isNull(AttendanceGroupMemberEntity::getEffectiveEndDate)
                        .or()
                        .ge(AttendanceGroupMemberEntity::getEffectiveEndDate, startDate)));
        return members == null ? Collections.emptyList() : members;
    }

    /**
     * 查询考勤统计打卡记录。
     *
     * @param employeeIds 员工ID列表
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 打卡记录Map
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),Collectors(JDK)
     */
    private Map<String, AttendanceRecordEntity> listDashboardRecords(List<Long> employeeIds, LocalDate startDate, LocalDate endDate) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return Map.of();
        }
        return attendanceRecordMapper.selectList(new LambdaQueryWrapper<AttendanceRecordEntity>()
                        .in(AttendanceRecordEntity::getEmployeeId, employeeIds)
                        .between(AttendanceRecordEntity::getRecordDate, startDate, endDate))
                .stream()
                .collect(Collectors.toMap(record -> buildRecordKey(record.getEmployeeId(), record.getRecordDate()),
                        record -> record, (a, b) -> a));
    }

    /**
     * 查询考勤统计请假记录。
     *
     * @param employeeIds 员工ID列表
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 请假记录列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),Collections(JDK)
     */
    private List<LeaveRequestEntity> listDashboardLeaves(List<Long> employeeIds, LocalDate startDate, LocalDate endDate) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return List.of();
        }
        List<LeaveRequestEntity> leaves = leaveRequestMapper.selectList(new LambdaQueryWrapper<LeaveRequestEntity>()
                .in(LeaveRequestEntity::getEmployeeId, employeeIds)
                .eq(LeaveRequestEntity::getApprovalStatus, 2)
                .le(LeaveRequestEntity::getStartTime, endDate.atTime(LocalTime.MAX))
                .ge(LeaveRequestEntity::getEndTime, startDate.atStartOfDay()));
        return leaves == null ? Collections.emptyList() : leaves;
    }

    /**
     * 构建请假日期Key集合。
     *
     * @param leaves    请假记录
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 请假日期Key集合
     * 本方法使用的工具类: Set(JDK)
     */
    private Set<String> buildLeaveDateKeys(List<LeaveRequestEntity> leaves, LocalDate startDate, LocalDate endDate) {
        Set<String> leaveDateKeys = new HashSet<>();
        for (LeaveRequestEntity leave : leaves) {
            if (leave.getStartTime() == null || leave.getEndTime() == null) {
                continue;
            }
            LocalDate leaveStart = leave.getStartTime().toLocalDate().isBefore(startDate)
                    ? startDate
                    : leave.getStartTime().toLocalDate();
            LocalDate leaveEnd = leave.getEndTime().toLocalDate().isAfter(endDate)
                    ? endDate
                    : leave.getEndTime().toLocalDate();
            leaveStart.datesUntil(leaveEnd.plusDays(1))
                    .forEach(date -> leaveDateKeys.add(buildRecordKey(leave.getEmployeeId(), date)));
        }
        return leaveDateKeys;
    }

    /**
     * 构建工作日列表。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 工作日列表
     * 本方法使用的工具类: Stream(JDK)
     */
    private List<LocalDate> listWorkdays(LocalDate startDate, LocalDate endDate) {
        return attendanceCalendarConfigService.listWorkdays(startDate, endDate);
    }

    /**
     * 构建考勤统计累加器。
     *
     * @param employees     员工快照
     * @param members       成员关系
     * @param recordMap     打卡记录Map
     * @param leaveDateKeys 请假日期Key集合
     * @param workdays      工作日列表
     * @return 统计累加器
     * 本方法使用的工具类: LinkedHashMap(JDK),HashMap(JDK)
     */
    private AttendanceDashboardAccumulator buildDashboardAccumulator(List<EmployeeSnapshotEntity> employees,
                                                                     List<AttendanceGroupMemberEntity> members,
                                                                     Map<String, AttendanceRecordEntity> recordMap,
                                                                     Set<String> leaveDateKeys,
                                                                     List<LocalDate> workdays) {
        AttendanceDashboardAccumulator accumulator = new AttendanceDashboardAccumulator(workdays);
        for (EmployeeSnapshotEntity employee : employees) {
            DashboardEmployeeStats employeeStats = accumulator.employeeStats.computeIfAbsent(employee.getId(),
                    employeeId -> new DashboardEmployeeStats(employee));
            DashboardMutableStats deptStats = accumulator.deptStats.computeIfAbsent(employee.getDeptId(),
                    deptId -> new DashboardMutableStats());
            for (LocalDate date : workdays) {
                if (!isMemberEffectiveOnDate(members, employee.getId(), date)) {
                    continue;
                }
                String recordKey = buildRecordKey(employee.getId(), date);
                AttendanceRecordEntity record = recordMap.get(recordKey);
                boolean leave = leaveDateKeys.contains(recordKey);
                AttendanceStatusView statusView = resolveGroupRecordStatus(record);
                DashboardMutableStats dailyStats = accumulator.dailyStats.get(date);
                accumulateDashboardStats(dailyStats, deptStats, employeeStats, record, statusView, leave);
            }
        }
        return accumulator;
    }

    /**
     * 累加单日考勤统计。
     *
     * @param dailyStats   日期统计
     * @param deptStats    部门统计
     * @param employeeStats 员工统计
     * @param record       打卡记录
     * @param statusView   综合状态
     * @param leave        是否请假
     * @return 无
     * 本方法使用的工具类: 无
     */
    private void accumulateDashboardStats(DashboardMutableStats dailyStats,
                                          DashboardMutableStats deptStats,
                                          DashboardEmployeeStats employeeStats,
                                          AttendanceRecordEntity record,
                                          AttendanceStatusView statusView,
                                          boolean leave) {
        dailyStats.expectedDays++;
        deptStats.expectedDays++;
        boolean actual = record != null
                && (record.getClockInTime() != null || record.getClockOutTime() != null)
                && ("NORMAL".equals(statusView.status())
                || "LATE".equals(statusView.status())
                || "EARLY_LEAVE".equals(statusView.status())
                || "ABNORMAL".equals(statusView.status()));
        if (actual) {
            dailyStats.actualDays++;
            deptStats.actualDays++;
        }
        if (record != null && "LATE".equals(record.getClockInStatus())) {
            dailyStats.lateCount++;
            deptStats.lateCount++;
            employeeStats.lateCount++;
        }
        if (record != null && "EARLY_LEAVE".equals(record.getClockOutStatus())) {
            dailyStats.earlyLeaveCount++;
            deptStats.earlyLeaveCount++;
            employeeStats.earlyLeaveCount++;
        }
        boolean absent = (record == null || (record.getClockInTime() == null && record.getClockOutTime() == null)) && !leave;
        if (absent) {
            dailyStats.absentCount++;
            deptStats.absentCount++;
            employeeStats.absentCount++;
        }
    }

    /**
     * 构建考勤统计看板返回值。
     *
     * @param accumulator   统计累加器
     * @param leaveCount    请假天数
     * @param deptNameCache 部门名称缓存
     * @return 考勤统计看板
     * 本方法使用的工具类: Comparator(JDK),BigDecimal(JDK)
     */
    private AttendanceSummaryDashboardVO buildDashboardVO(AttendanceDashboardAccumulator accumulator,
                                                          BigDecimal leaveCount,
                                                          Map<Long, String> deptNameCache) {
        // 计算考勤统计看板数据
        int expectedDays = accumulator.dailyStats.values().stream().
                mapToInt(stats -> stats.expectedDays).sum();
        // 计算实际出勤天数
        int actualDays = accumulator.dailyStats.values().stream()
                .mapToInt(stats -> stats.actualDays).sum();
        // 计算迟到次数
        int lateCount = accumulator.dailyStats.values().stream()
                .mapToInt(stats -> stats.lateCount).sum();
        //计算早退次数
        int earlyLeaveCount = accumulator.dailyStats.values().stream()

                .mapToInt(stats -> stats.earlyLeaveCount).sum();
        // 计算缺勤次数
        int absentCount = accumulator.dailyStats.values().stream()
                .mapToInt(stats -> stats.absentCount).sum();
        // 计算每日考勤趋势
        List<AttendanceTrendPointVO> dailyTrend = accumulator.dailyStats.entrySet().stream()
                .map(entry -> AttendanceTrendPointVO.builder()
                        .date(entry.getKey())
                        .actualDays(entry.getValue().actualDays)
                        .expectedDays(entry.getValue().expectedDays)
                        .attendanceRate(calculateAttendanceRate(entry.getValue().actualDays, entry.getValue().expectedDays))
                        .build())
                .toList();
        // 计算部门考勤分布
        List<AttendanceDeptDistributionVO> deptDistribution = accumulator.deptStats.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey() == null ? Long.MAX_VALUE : entry.getKey()))
                .map(entry -> AttendanceDeptDistributionVO.builder()
                        .deptId(entry.getKey())
                        .deptName(resolveDeptName(entry.getKey(), deptNameCache))
                        .actualDays(entry.getValue().actualDays)
                        .expectedDays(entry.getValue().expectedDays)
                        .attendanceRate(calculateAttendanceRate(entry.getValue().actualDays, entry.getValue().expectedDays))
                        .build())
                .toList();
        // 计算员工考勤排名
        List<AttendanceEmployeeRankingVO> employeeRanking = accumulator.employeeStats.values().stream()
                .filter(stats -> stats.abnormalCount() > 0)
                .sorted(Comparator.comparingInt(DashboardEmployeeStats::abnormalCount).reversed()
                        .thenComparing(Comparator.comparingInt((DashboardEmployeeStats stats) -> stats.absentCount).reversed())
                        .thenComparing(Comparator.comparingInt((DashboardEmployeeStats stats) -> stats.lateCount).reversed()))
                .limit(DASHBOARD_RANKING_LIMIT)
                .map(stats -> AttendanceEmployeeRankingVO.builder()
                        .employeeId(stats.employee.getId())
                        .employeeName(stats.employee.getEmployeeName())
                        .employeeNo(stats.employee.getEmployeeNo())
                        .deptName(resolveDeptName(stats.employee.getDeptId(), deptNameCache))
                        .abnormalCount(stats.abnormalCount())
                        .lateCount(stats.lateCount)
                        .earlyLeaveCount(stats.earlyLeaveCount)
                        .absentCount(stats.absentCount)
                        .build())
                .toList();
        return AttendanceSummaryDashboardVO.builder()
                .expectedDays(expectedDays)
                .actualDays(actualDays)
                .lateCount(lateCount)
                .earlyLeaveCount(earlyLeaveCount)
                .absentCount(absentCount)
                .leaveCount(leaveCount)
                .dailyTrend(dailyTrend)
                .deptDistribution(deptDistribution)
                .exceptionPie(buildExceptionPie(lateCount, earlyLeaveCount, absentCount))
                .employeeRanking(employeeRanking)
                .build();
    }

    /**
     * 构建空考勤统计看板。
     *
     * @param workdays 工作日列表
     * @return 空考勤统计看板
     * 本方法使用的工具类: AttendanceSummaryDashboardVO(本模块vo包)
     */
    private AttendanceSummaryDashboardVO emptyDashboard(List<LocalDate> workdays) {
        // 计算每日考勤趋势
        List<AttendanceTrendPointVO> dailyTrend = workdays.stream()
                .map(date -> AttendanceTrendPointVO.builder()
                        .date(date)
                        .actualDays(0)
                        .expectedDays(0)
                        .attendanceRate(BigDecimal.ZERO)
                        .build())
                .toList();
        return AttendanceSummaryDashboardVO.builder()
                .expectedDays(0)
                .actualDays(0)
                .lateCount(0)
                .earlyLeaveCount(0)
                .absentCount(0)
                .leaveCount(BigDecimal.ZERO)
                .dailyTrend(dailyTrend)
                .deptDistribution(List.of())
                .exceptionPie(buildExceptionPie(0, 0, 0))
                .employeeRanking(List.of())
                .build();
    }

    /**
     * 构建异常占比数据。
     *
     * @param lateCount       迟到次数
     * @param earlyLeaveCount 早退次数
     * @param absentCount     缺勤次数
     * @return 异常占比数据
     * 本方法使用的工具类: List(JDK)
     */
    private List<AttendanceExceptionPieVO> buildExceptionPie(int lateCount, int earlyLeaveCount, int absentCount) {
        return List.of(
                //迟到
                AttendanceExceptionPieVO.builder()
                        .type("LATE")
                        .count(lateCount)
                        .build(),
                //早退
                AttendanceExceptionPieVO.builder()
                        .type("EARLY_LEAVE")
                        .count(earlyLeaveCount).build(),
                //缺勤
                AttendanceExceptionPieVO.builder()
                        .type("ABSENCE")
                        .count(absentCount)
                        .build()
        );
    }

    /**
     * 计算出勤率。
     *
     * @param actualDays   实际出勤人天
     * @param expectedDays 应出勤人天
     * @return 出勤率
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private BigDecimal calculateAttendanceRate(int actualDays, int expectedDays) {
        if (expectedDays <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(actualDays)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(expectedDays), 2, RoundingMode.HALF_UP);
    }

    /**
     * 解析请假管理查询月份。
     *
     * @param yearMonth 月份
     * @return 年月
     * 本方法使用的工具类: YearMonth(JDK),GlobalException(hrms-common)
     */
    private YearMonth parseLeaveManageYearMonth(String yearMonth) {
        if (StrUtil.isBlank(yearMonth)) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException ex) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "yearMonth格式必须为yyyy-MM");
        }
    }

    /**
     * 解析请假管理数据范围。
     *
     * @param requestedDeptId 请求部门ID
     * @return 请假管理范围
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),RoleService(hrms-system-auth)
     */
    private LeaveManageScope resolveLeaveManageScope(Long requestedDeptId) {
        Long currentUserId = SecurityContextHolder.getUserId();
        List<RoleEntity> roles = roleService.getRolesByUserId(currentUserId);
        // 判断是否拥有请假管理全量权限
        if (hasLeaveManageFullScope(roles)) {
            return new LeaveManageScope(requestedDeptId == null ? null : Set.of(requestedDeptId));
        }
        // 判断是否拥有请假管理主管权限
        if (!hasLeaveManageManagerScope(roles)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "无权查看请假管理列表");
        }
        Long currentDeptId = SecurityContextHolder.getDeptId();
        // 尝试从当前用户快照中获取部门ID
        if (currentDeptId == null) {
            currentDeptId = getCurrentEmployeeSnapshot().getDeptId();
        }
        if (currentDeptId == null) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "当前用户未配置部门，无法查看请假管理列表");
        }
        if (requestedDeptId != null && !requestedDeptId.equals(currentDeptId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "无权查看该部门请假记录");
        }
        return new LeaveManageScope(Set.of(currentDeptId));
    }

    /**
     * 判断是否拥有请假管理全量权限。
     *
     * @param roles 角色列表
     * @return 是否拥有全量权限
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private boolean hasLeaveManageFullScope(List<RoleEntity> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role != null // 角色不为空
                && StrUtil.isNotBlank(role.getRoleCode()) // 角色代码不为空
                && LEAVE_MANAGE_FULL_SCOPE_ROLE_CODES.contains(role.getRoleCode().toUpperCase())// 角色代码在全量权限集合中
                );
    }

    /**
     * 判断是否拥有请假管理主管权限。
     *
     * @param roles 角色列表
     * @return 是否拥有主管权限
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private boolean hasLeaveManageManagerScope(List<RoleEntity> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream().anyMatch(role -> role != null
                && StrUtil.isNotBlank(role.getRoleCode())
                && LEAVE_MANAGE_MANAGER_ROLE_CODE.equals(role.getRoleCode().toUpperCase()));
    }

    /**
     * 查询请假管理员工范围。
     *
     * @param targetDeptIds 目标部门ID集合，null表示全量
     * @param keyword       员工关键字
     * @return 员工快照列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),StrUtil(hutool),Collections(JDK)
     */
    private List<EmployeeSnapshotEntity> listLeaveManageEmployees(Set<Long> targetDeptIds, String keyword) {
        //若目标部门为空，则返回空列表
        if (targetDeptIds != null && targetDeptIds.isEmpty()) {
            return List.of();
        }
        // 构建查询条件
        LambdaQueryWrapper<EmployeeSnapshotEntity> wrapper = new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                .ne(EmployeeSnapshotEntity::getEmploymentStatus, 4)
                .and(StrUtil.isNotBlank(keyword), query -> query
                        .like(EmployeeSnapshotEntity::getEmployeeName, keyword.trim())
                        .or()
                        .like(EmployeeSnapshotEntity::getEmployeeNo, keyword.trim()));
        // 添加部门过滤条件
        if (targetDeptIds != null) {
            wrapper.in(EmployeeSnapshotEntity::getDeptId, targetDeptIds);
        }
        List<EmployeeSnapshotEntity> employees = employeeSnapshotMapper.selectList(wrapper);
        return employees == null ? Collections.emptyList() : employees;
    }

    /**
     * 加载请假类型描述映射。
     *
     * @return 请假类型描述映射
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),Collectors(JDK)
     */
    private Map<String, String> loadLeaveTypeDescMap() {
        List<DictDataEntity> dictList = dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                .eq(DictDataEntity::getDictType, "leave_type")
                .eq(DictDataEntity::getStatus, 1)
                .eq(DictDataEntity::getIsDeleted, 0));
        if (dictList == null || dictList.isEmpty()) {
            return Map.of();
        }
        return dictList.stream()
                .collect(Collectors.toMap(DictDataEntity::getDictValue, DictDataEntity::getDictLabel, (a, b) -> a));
    }

    /**
     * 构建请假管理列表项。
     *
     * @param leave              请假记录
     * @param employee           员工快照
     * @param deptNameCache      部门名称缓存
     * @param leaveTypeDescMap   请假类型描述映射
     * @param approvalBriefCache 审批摘要缓存
     * @param currentUserId      当前用户ID
     * @return 请假管理列表项
     * 本方法使用的工具类: AttendanceLeaveManageItemVO(本模块vo包)
     */
    private AttendanceLeaveManageItemVO buildLeaveManageItemVO(LeaveRequestEntity leave,
                                                               EmployeeSnapshotEntity employee,
                                                               Map<Long, String> deptNameCache,
                                                               Map<String, String> leaveTypeDescMap,
                                                               Map<Long, ApprovalBrief> approvalBriefCache,
                                                               Long currentUserId) {
        //解析审批摘要
        ApprovalBrief approvalBrief = resolveApprovalBrief(leave.getApprovalInstanceId(), approvalBriefCache, currentUserId);
        Long deptId = employee == null ? null : employee.getDeptId();
        //请假类型
        String leaveTypeDesc = leaveTypeDescMap.getOrDefault(leave.getLeaveType(), leave.getLeaveType());
        return AttendanceLeaveManageItemVO.builder()
                .id(leave.getId())
                .employeeId(leave.getEmployeeId())
                .employeeName(employee == null ? null : employee.getEmployeeName())
                .employeeNo(employee == null ? null : employee.getEmployeeNo())
                .deptId(deptId)
                .deptName(resolveDeptName(deptId, deptNameCache))
                .leaveType(leave.getLeaveType())
                .leaveTypeDesc(leaveTypeDesc)
                .startTime(leave.getStartTime())
                .endTime(leave.getEndTime())
                .totalDays(leave.getTotalDays())
                .leaveReason(leave.getLeaveReason())
                .approvalStatus(leave.getApprovalStatus())
                .approvalStatusDesc(resolveLeaveApprovalStatusDesc(leave.getApprovalStatus()))
                .approvalInstanceId(leave.getApprovalInstanceId())
                .currentNodeName(approvalBrief.currentNodeName())
                .currentApproverName(approvalBrief.currentApproverName())
                .createTime(leave.getCreateTime())
                .build();
    }

    /**
     * 解析审批摘要。
     *
     * @param approvalInstanceId 审批实例ID
     * @param cache              审批摘要缓存
     * @param currentUserId      当前用户ID
     * @return 审批摘要
     * 本方法使用的工具类: ApprovalTaskService(hrms-business-approval)
     */
    private ApprovalBrief resolveApprovalBrief(Long approvalInstanceId, Map<Long, ApprovalBrief> cache, Long currentUserId) {
        if (approvalInstanceId == null) {
            return ApprovalBrief.empty();
        }
        //若
        if (cache.containsKey(approvalInstanceId)) {
            return cache.get(approvalInstanceId);
        }
        try {
            ApprovalDetailVO detail = approvalTaskService.getDetail(approvalInstanceId, currentUserId);
            ApprovalBrief brief = extractApprovalBrief(detail);
            cache.put(approvalInstanceId, brief);
            return brief;
        } catch (Exception ex) {
            log.warn("resolve leave approval brief failed, approvalInstanceId={}", approvalInstanceId, ex);
            ApprovalBrief brief = ApprovalBrief.empty();
            cache.put(approvalInstanceId, brief);
            return brief;
        }
    }

    /**
     * 从审批详情提取当前节点摘要。
     *
     * @param detail 审批详情
     * @return 审批摘要
     * 本方法使用的工具类: ApprovalDetailVO(hrms-business-approval)
     */
    private ApprovalBrief extractApprovalBrief(ApprovalDetailVO detail) {
        if (detail == null || detail.getApprovalNodes() == null) {
            return ApprovalBrief.empty();
        }
        return detail.getApprovalNodes().stream()
                .filter(node -> "current".equals(node.getStatus()))
                .findFirst()
                .map(node -> new ApprovalBrief(node.getNodeName(), node.getOperatorName()))
                .orElse(ApprovalBrief.empty());
    }

    /**
     * 解析请假审批状态描述。
     *
     * @param approvalStatus 审批状态
     * @return 状态描述
     * 本方法使用的工具类: 无
     */
    private String resolveLeaveApprovalStatusDesc(Integer approvalStatus) {
        if (approvalStatus == null) {
            return "未知";
        }
        return switch (approvalStatus) {
            case 0 -> "草稿";
            case 1 -> "审批中";
            case 2 -> "已通过";
            case 3 -> "已拒绝";
            case 4 -> "已撤回";
            default -> "未知";
        };
    }

    /**
     * 规范化当前页码。
     *
     * @param pageNum 当前页码
     * @return 当前页码
     * 本方法使用的工具类: 无
     */
    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? 1 : pageNum;
    }

    /**
     * 规范化每页大小。
     *
     * @param pageSize 每页大小
     * @return 每页大小
     * 本方法使用的工具类: 无
     */
    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize <= 0 ? 10 : pageSize;
    }

    /**
     * 考勤组记录查询日期范围。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     */
    private record AttendanceDateRange(LocalDate startDate, LocalDate endDate) {
    }

    /**
     * 考勤综合状态视图。
     *
     * @param status     状态编码
     * @param statusName 状态名称
     */
    private record AttendanceStatusView(String status, String statusName) {
    }

    /**
     * 考勤打卡。
     * @param requestDTO 打卡参数
     * @param clientIp 客户端IP
     * @return 打卡结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceClockVO clock(AttendanceClockRequestDTO requestDTO, String clientIp) {
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        LocalDateTime now = LocalDateTime.now();
        LocalDate recordDate = now.toLocalDate();
        AttendanceGroupEntity group = resolveEmployeeAttendanceGroup(employee.getId(), recordDate);
        // 法定节假日优先于按星期计算的工作日，命中时直接拒绝打卡。
        validateClockWorkday(recordDate);
        //验证打卡时间范围
        validateClockRange(group, requestDTO, clientIp);

        AttendanceRecordEntity existing = attendanceRecordMapper.selectByEmployeeAndDate(employee.getId(), recordDate);
        //确定打卡周期
        ClockPeriodEnum period = resolveClockPeriod(requestDTO, existing);
        //计算打卡状态
        String status = calculateClockStatus(group, period, now.toLocalTime());
        //构建打卡记录
        AttendanceRecordEntity record = buildClockRecord(employee.getId(), group.getId(), recordDate, now, period, status, requestDTO, clientIp);
        //持久化打卡记录
        persistClockRecord(existing, record, period);
        //删除个人月历缓存
        evictCalendarCache(employee.getId(), recordDate);
        //构建打卡创建事件
        AttendanceClockCreatedEvent event = buildClockCreatedEvent(record, period, status, now, requestDTO.getDeviceInfo());
        //发布打卡创建事件
        publishClockCreatedEvent(event);
        return buildClockVO(record, period, status, now);
    }

    /**
     * 校验指定日期是否允许打卡。
     *
     * @param recordDate 打卡业务日期
     */
    private void validateClockWorkday(LocalDate recordDate) {
        if (!attendanceCalendarConfigService.isWorkday(recordDate)) {
            throw new GlobalException(ATTENDANCE_CLOCK_HOLIDAY_SKIP);
        }
    }

    /**
     * 获取个人日历。
     * @param yearMonth 年月，格式为 yyyy-MM
     * @return 日历数据
     */
    @Override
    public AttendanceCalendarVO getMyCalendar(String yearMonth) {
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        //解析年月
        YearMonth parsedMonth = YearMonth.parse(yearMonth);
        //构建缓存键
        String cacheKey = AttendanceCacheKeys.monthCalendar(employee.getId(), parsedMonth.toString());
        //从缓存获取日历数据
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        //如果缓存存在则返回缓存数据
        AttendanceGroupEntity currentGroup = resolveCurrentAttendanceGroupOrNull(employee.getId(), LocalDate.now());
        if (StrUtil.isNotBlank(cached)) {
            return AttendanceCalendarSummaryEnricher.enrich(
                    JSONUtil.toBean(cached, AttendanceCalendarVO.class),
                    currentGroup
            );
        }

        //构建日历数据
        AttendanceCalendarVO calendar = buildCalendarFromDatabase(employee.getId(), parsedMonth);
        //增强VO
        calendar = AttendanceCalendarSummaryEnricher.enrich(calendar, currentGroup);
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(calendar), Duration.ofHours(6));
        return calendar;
    }

    /**
     * 查询HR和主管考勤统计看板。
     *
     * @param yearMonth 月份，格式yyyy-MM
     * @param deptId    部门ID
     * @return 考勤统计看板
     * 本方法使用的工具类: YearMonth(JDK),SecurityContextHolder(hrms-common),RoleService(hrms-system-auth)
     */
    @Override
    public AttendanceSummaryDashboardVO getSummaryDashboard(String yearMonth, Long deptId, Boolean refreshCache) {
        String cacheKey = AttendanceCacheKeys.summaryDashboard(
                buildAttendanceSummaryDashboardCacheKey(yearMonth, deptId)
        );
        if (Boolean.TRUE.equals(refreshCache)) {
            deleteCacheKey(cacheKey, "delete attendance summary dashboard cache failed, cacheKey={}");
        } else {
            AttendanceSummaryDashboardVO cachedResult = loadCacheValue(
                    cacheKey,
                    AttendanceSummaryDashboardVO.class,
                    "load attendance summary dashboard cache failed, cacheKey={}"
            );
            if (cachedResult != null) {
                return cachedResult;
            }
        }
        YearMonth parsedMonth = parseDashboardYearMonth(yearMonth);
        LocalDate startDate = parsedMonth.atDay(1);
        LocalDate endDate = parsedMonth.atEndOfMonth();
        AttendanceDashboardScope scope = resolveDashboardScope(deptId);
        List<LocalDate> workdays = listWorkdays(startDate, endDate);
        Map<Long, String> deptNameCache = new HashMap<>();
        List<EmployeeSnapshotEntity> employees = listDashboardEmployees(scope.targetDeptIds());
        //如果部门下没有员工则返回空看板
        if (employees.isEmpty()) {
            AttendanceSummaryDashboardVO emptyResult = emptyDashboard(workdays);
            saveCacheValue(cacheKey, emptyResult, "save attendance summary dashboard cache failed, cacheKey={}");
            return emptyResult;
        }
        List<Long> employeeIds = employees.stream().map(EmployeeSnapshotEntity::getId).distinct().toList();
        List<AttendanceGroupMemberEntity> members = listDashboardMembers(employeeIds, startDate, endDate);
        //如果部门下没有成员则返回空看板
        if (members.isEmpty()) {
            AttendanceSummaryDashboardVO emptyResult = emptyDashboard(workdays);
            saveCacheValue(cacheKey, emptyResult, "save attendance summary dashboard cache failed, cacheKey={}");
            return emptyResult;
        }
        Set<Long> memberEmployeeIds = members.stream()
                .map(AttendanceGroupMemberEntity::getEmployeeId)
                .collect(Collectors.toSet());
        List<EmployeeSnapshotEntity> statEmployees = employees.stream()
                .filter(employee -> memberEmployeeIds.contains(employee.getId()))
                .toList();
        //如果成员中没有需要统计的员工则返回空看板
        if (statEmployees.isEmpty()) {
            AttendanceSummaryDashboardVO emptyResult = emptyDashboard(workdays);
            saveCacheValue(cacheKey, emptyResult, "save attendance summary dashboard cache failed, cacheKey={}");
            return emptyResult;
        }
        List<Long> statEmployeeIds = statEmployees.stream().map(EmployeeSnapshotEntity::getId).distinct().toList();
        //查询考勤统计打卡记录
        Map<String, AttendanceRecordEntity> recordMap = listDashboardRecords(statEmployeeIds, startDate, endDate);
        //查询考勤统计请假记录
        List<LeaveRequestEntity> leaves = listDashboardLeaves(statEmployeeIds, startDate, endDate);
        //构建请假日期键集合
        Set<String> leaveDateKeys = buildLeaveDateKeys(leaves, startDate, endDate);
        BigDecimal leaveCount = leaves.stream()
                .map(LeaveRequestEntity::getTotalDays)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //构建考勤统计累加器
        AttendanceDashboardAccumulator accumulator = buildDashboardAccumulator(
                statEmployees, members, recordMap, leaveDateKeys, workdays);
        AttendanceSummaryDashboardVO result = buildDashboardVO(accumulator, leaveCount, deptNameCache);
        saveCacheValue(cacheKey, result, "save attendance summary dashboard cache failed, cacheKey={}");
        return result;
    }

    /**
     * 分页查询管理侧请假记录。
     *
     * @param queryDTO 查询参数
     * @return 请假管理分页列表
     * 本方法使用的工具类: Page(mybatis-plus),PageResult(hrms-common),YearMonth(JDK)
     */
    @Override
    public PageResult<AttendanceLeaveManageItemVO> pageLeaveManageList(AttendanceLeaveManageQueryDTO queryDTO) {
        //解析查询参数
        AttendanceLeaveManageQueryDTO safeQuery = queryDTO == null ? new AttendanceLeaveManageQueryDTO() : queryDTO;
        //解析年月
        YearMonth yearMonth = parseLeaveManageYearMonth(safeQuery.getYearMonth());
        //解析年月
        LocalDate monthStart = yearMonth.atDay(1);
        //获取月结束日期
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        //解析部门ID
        LeaveManageScope scope = resolveLeaveManageScope(safeQuery.getDeptId());
        //查询请假管理员工范围
        List<EmployeeSnapshotEntity> employees = listLeaveManageEmployees(scope.targetDeptIds(), safeQuery.getKeyword());
        //解析页码
        int pageNum = normalizePageNum(safeQuery.getPageNum());
        //解析每页大小
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        String cacheKey = AttendanceCacheKeys.leaveManagePage(
                buildAttendanceLeaveManagePageCacheKey(safeQuery, yearMonth, pageNum, pageSize)
        );
        if (Boolean.TRUE.equals(safeQuery.getRefreshCache())) {
            deleteCacheKey(cacheKey, "delete attendance leave manage page cache failed, cacheKey={}");
        } else {
            PageResult<AttendanceLeaveManageItemVO> cachedResult = loadCacheValue(
                    cacheKey,
                    new TypeReference<PageResult<AttendanceLeaveManageItemVO>>() {
                    },
                    "load attendance leave manage page cache failed, cacheKey={}"
            );
            if (cachedResult != null) {
                return cachedResult;
            }
        }
        if (employees.isEmpty()) {
            PageResult<AttendanceLeaveManageItemVO> emptyResult = PageResult.of(List.of(), 0, pageNum, pageSize);
            saveCacheValue(cacheKey, emptyResult, "save attendance leave manage page cache failed, cacheKey={}");
            return emptyResult;
        }
        //构建员工ID到员工信息的映射
        Map<Long, EmployeeSnapshotEntity> employeeMap = employees.stream()
                .collect(Collectors.toMap(EmployeeSnapshotEntity::getId, employee -> employee, (a, b) -> a));
        //分页查询考勤管理列表
        Page<LeaveRequestEntity> page = Page.of(pageNum, pageSize);
        LambdaQueryWrapper<LeaveRequestEntity> wrapper = new LambdaQueryWrapper<LeaveRequestEntity>()
                .in(LeaveRequestEntity::getEmployeeId, employeeMap.keySet())
                .le(LeaveRequestEntity::getStartTime, monthEnd.atTime(LocalTime.MAX))
                .ge(LeaveRequestEntity::getEndTime, monthStart.atStartOfDay())
                .eq(safeQuery.getApprovalStatus() != null, LeaveRequestEntity::getApprovalStatus, safeQuery.getApprovalStatus())
                .orderByDesc(LeaveRequestEntity::getCreateTime)
                .orderByDesc(LeaveRequestEntity::getId);
        Page<LeaveRequestEntity> resultPage = leaveRequestMapper.selectPage(page, wrapper);
        //构建请假管理列表VO
        Map<Long, String> deptNameCache = new HashMap<>();
        //加载请假类型描述映射
        Map<String, String> leaveTypeDescMap = loadLeaveTypeDescMap();
        //审批缓存
        Map<Long, ApprovalBrief> approvalBriefCache = new HashMap<>();
        Long currentUserId = SecurityContextHolder.getUserId();

        List<AttendanceLeaveManageItemVO> records = resultPage.getRecords().stream()
                .map(leave -> buildLeaveManageItemVO(leave, employeeMap.get(leave.getEmployeeId()),
                        deptNameCache, leaveTypeDescMap, approvalBriefCache, currentUserId))
                .toList();
        PageResult<AttendanceLeaveManageItemVO> result = PageResult.of(records, resultPage.getTotal(), pageNum, pageSize);
        saveCacheValue(cacheKey, result, "save attendance leave manage page cache failed, cacheKey={}");
        return result;
    }


    /**
     * 创建补卡申请。
     * @param requestDTO 创建参数
     * @return 补卡申请结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceCorrectionCreateVO createCorrection(AttendanceCorrectionCreateRequestDTO requestDTO) {
        //获取当前员工离职信息
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        //解析补卡类型
        ClockPeriodEnum period = ClockPeriodEnum.parse(requestDTO.getClockType());
        if (period == null) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "补卡类型不正确");
        }
        // 检查补卡记录是否重复
        checkCorrectionDuplicate(employee.getId(), requestDTO.getDate(), period);
        // 获取或创建补卡记录
        AttendanceRecordEntity record = getOrCreateCorrectionRecord(employee.getId(), requestDTO.getDate());

        AttendanceCorrectionEntity correction = new AttendanceCorrectionEntity();
        correction.setEmployeeId(employee.getId());
        correction.setRecordId(record.getId());
        correction.setCorrectionDate(requestDTO.getDate());
        correction.setCorrectionType(period.name());
        correction.setCorrectionReason(requestDTO.getReason());
        correction.setApprovalStatus(1);
        attendanceCorrectionMapper.insert(correction);

        // 跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起补卡审批。
        Long approvalInstanceId = approvalEngine.startApproval(
                ApprovalTypeEnum.CORRECTION.getCode(),
                correction.getId(),
                JSONUtil.toJsonStr(correction),
                SecurityContextHolder.getUserId(),
                employee.getDeptId(),
                employee.getId()
        );
        correction.setApprovalInstanceId(approvalInstanceId);
        attendanceCorrectionMapper.updateById(correction);
        attendanceRecordMapper.updateCorrectionStatus(record.getId(), "PENDING");
        //
        evictCalendarCache(employee.getId(), requestDTO.getDate());
        return buildCorrectionCreateVO(correction);
    }

    /**
     * 列出假期类型。
     * @return 假期类型列表
     */
    @Override
    public List<LeaveTypeVO> listLeaveTypes() {
        return dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                        .eq(DictDataEntity::getDictType, "hr_leave_type")
                        .eq(DictDataEntity::getStatus, 1)
                        .eq(DictDataEntity::getIsDeleted, 0)
                        .orderByAsc(DictDataEntity::getSort)
                        .orderByAsc(DictDataEntity::getId))
                .stream()
                .map(this::toLeaveTypeVO)
                .toList();
    }

    /**
     * 列出假期余额。
     * @return 假期余额列表
     */
    @Override
    public List<LeaveBalanceVO> listLeaveBalances() {
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        int currentYear = LocalDate.now().getYear();
        //查询当前员工指定年份的假期余额
        List<LeaveBalanceEntity> balances = leaveBalanceMapper.selectList(new LambdaQueryWrapper<LeaveBalanceEntity>()
                .eq(LeaveBalanceEntity::getEmployeeId, employee.getId())
                .eq(LeaveBalanceEntity::getBalanceYear, currentYear)
                .eq(LeaveBalanceEntity::getStatus, 1)
                .eq(LeaveBalanceEntity::getIsDeleted, 0)
                .orderByAsc(LeaveBalanceEntity::getLeaveType)
                .orderByAsc(LeaveBalanceEntity::getId));
        Map<String, LeaveBalanceVO> balanceMap = new LinkedHashMap<>();
        for (LeaveBalanceEntity balance : balances) {
            LeaveBalanceVO vo = toLeaveBalanceVO(balance);
            balanceMap.putIfAbsent(vo.getLeaveType(), vo);
        }
        // 为缺失的基础假期类型补默认 0 余额
        appendDefaultLeaveBalance(balanceMap, "annual", "年假");
        appendDefaultLeaveBalance(balanceMap, "sick", "病假");
        appendDefaultLeaveBalance(balanceMap, "personal", "事假");
        return List.copyOf(balanceMap.values());
    }

//    /**
//     * 已停用：临时计算年假总额度，仅作历史临时算法参考；当前已替换为 hr_leave_balance 表读取。
//     *
//     * @param employee 员工快照
//     * @return 年假总额度
//     * 本方法使用的工具类: BigDecimal(JDK)
//     */
//    private BigDecimal calculateAnnualTotal(EmployeeSnapshotEntity employee) {
//        if (employee.getHireDate() == null || employee.getHireDate().isAfter(LocalDate.now())) {
//            return BigDecimal.ZERO;
//        }
//        int months = Math.max(1, LocalDate.now().getMonthValue() - employee.getHireDate().getMonthValue() + 1);
//        return BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(months))
//                .divide(BigDecimal.valueOf(12), 1, RoundingMode.DOWN);
//    }

    /**
     * 将假期余额表记录转换为接口视图。
     *
     * @param balance 假期余额记录
     * @return 假期余额视图
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private LeaveBalanceVO toLeaveBalanceVO(LeaveBalanceEntity balance) {
        String leaveType = normalizeLeaveType(balance.getLeaveType());
        return LeaveBalanceVO.builder()
                .leaveType(leaveType)
                .leaveTypeName(resolveLeaveTypeName(leaveType))
                .totalDays(defaultZero(balance.getTotalDays()))
                .usedDays(defaultZero(balance.getUsedDays()))
                .remainingDays(defaultZero(balance.getRemainingDays()))
                .build();
    }

    /**
     * 为缺失的基础假期类型补默认 0 余额。
     *
     * @param balanceMap 已有余额映射
     * @param leaveType  假期类型
     * @param typeName   假期类型名称
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private void appendDefaultLeaveBalance(Map<String, LeaveBalanceVO> balanceMap, String leaveType, String typeName) {
        balanceMap.putIfAbsent(leaveType, buildLeaveBalance(leaveType, typeName, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    /**
     * 规范化假期类型为前端兼容的小写值。
     *
     * @param leaveType 数据库假期类型
     * @return 小写假期类型
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private String normalizeLeaveType(String leaveType) {
        return StrUtil.blankToDefault(leaveType, "").toLowerCase();
    }

    /**
     * 解析假期类型名称。
     *
     * @param leaveType 小写假期类型
     * @return 假期类型名称
     * 本方法使用的工具类: 无
     */
    private String resolveLeaveTypeName(String leaveType) {
        return switch (leaveType) {
            case "annual" -> "年假";
            case "sick" -> "病假";
            case "personal" -> "事假";
            case "compassionate" -> "调休";
            case "marriage" -> "婚假";
            case "maternity" -> "产假";
            case "funeral" -> "丧假";
            default -> leaveType;
        };
    }

    /**
     * 空金额按 0 处理。
     *
     * @param value 金额或天数
     * @return 非空金额或天数
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 构建假期余额。
     *
     * @param type      请假类型
     * @param name      类型名称
     * @param totalDays 总额度
     * @param usedDays  已用天数
     * @return 假期余额
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private LeaveBalanceVO buildLeaveBalance(String type, String name, BigDecimal totalDays, BigDecimal usedDays) {
        return LeaveBalanceVO.builder()
                .leaveType(type)
                .leaveTypeName(name)
                .totalDays(totalDays)
                .usedDays(usedDays)
                .remainingDays(totalDays.subtract(usedDays).max(BigDecimal.ZERO))
                .build();
    }

    /**
     * 创建请假申请。
     * @param requestDTO 创建参数
     * @return 请假申请结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveCreateVO createLeave(LeaveCreateRequestDTO requestDTO) {
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        String leaveType = resolveLeaveType(requestDTO);
        BigDecimal totalDays = calculateLeaveDays(requestDTO);
        // 验证请假天数
        if (totalDays.compareTo(BigDecimal.ZERO) <= 0 || totalDays.compareTo(BigDecimal.valueOf(30)) > 0) {
            throw new GlobalException(LEAVE_DAYS_INVALID);
        }
        LeaveRequestEntity entity = new LeaveRequestEntity();
        entity.setEmployeeId(employee.getId());
        entity.setLeaveType(leaveType);
        entity.setStartTime(toPeriodTime(requestDTO.getStartDate(), requestDTO.getStartPeriod(), true));
        entity.setEndTime(toPeriodTime(requestDTO.getEndDate(), requestDTO.getEndPeriod(), false));
        entity.setTotalDays(totalDays);
        entity.setTotalHours(totalDays.multiply(BigDecimal.valueOf(8)));
        entity.setLeaveReason(requestDTO.getReason());
        entity.setAttachmentUrl(resolveAttachment(requestDTO));
        entity.setApprovalStatus(1);
        leaveRequestMapper.insert(entity);

        //  跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起请假审批。
        Long approvalInstanceId = approvalEngine.startApproval(
                ApprovalTypeEnum.LEAVE_REQUEST.getCode(),
                entity.getId(),
                JSONUtil.toJsonStr(entity),
                SecurityContextHolder.getUserId(),
                employee.getDeptId(),
                employee.getId()
        );
        entity.setApprovalInstanceId(approvalInstanceId);
        leaveRequestMapper.updateById(entity);
        // 删除个人月历缓存
        evictCalendarCache(employee.getId(), requestDTO.getStartDate());
        return buildLeaveCreateVO(entity);
    }

    /**
     * 生成月度统计。
     * @param requestDTO 生成参数
     * @return 月度统计结果
     */
    /**
     * 快速审批通过请假申请。
     *
     * @param id 请假申请 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quickApproveLeaveRequest(Long id) {
        LeaveRequestEntity entity = leaveRequestMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "请假申请不存在");
        }
        if (entity.getApprovalStatus() == null || entity.getApprovalStatus() != 1) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "当前请假申请不是审批中状态，无法快速审批");
        }
        if (entity.getApprovalInstanceId() == null) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "当前请假申请无有效审批实例，无法快速审批");
        }
        Long pendingTaskId = approvalTaskService.getCurrentPendingTaskIdByInstanceId(entity.getApprovalInstanceId());
        if (pendingTaskId == null) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "当前审批实例不存在待办审批任务，无法快速审批");
        }
        approvalEngine.processAction(pendingTaskId, "approve", "快速审批通过", null);
        // 审批成功后清理相关查询缓存，避免页面继续展示旧状态。
        evictAttendanceQueryCachesForLeaveApproval();
    }

    @Override
    public MonthlyStatGenerateVO generateMonthlyStat(MonthlyStatGenerateRequestDTO requestDTO) {
        String lockKey = AttendanceCacheKeys.monthStatGenerateLock(requestDTO.getMonth());
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(locked)) {
            return MonthlyStatGenerateVO.builder()
                    .month(requestDTO.getMonth())
                    .employeeCount(0)
                    .success(false)
                    .build();
        }
        try {
            AttendanceMonthlyStatGenerateMessage message = AttendanceMonthlyStatGenerateMessage.builder()
                    .messageId(IdUtil.fastSimpleUUID())
                    .month(requestDTO.getMonth())
                    .employeeIds(requestDTO.getEmployeeIds())
                    .build();
            attendanceMonthlyStatGenerateProducer.send(message);
        } catch (Exception ex) {
            stringRedisTemplate.delete(lockKey);
            log.warn("publish attendance.stat.monthly.generate failed, request={}", JSONUtil.toJsonStr(requestDTO), ex);
            if (ex instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new GlobalException(ErrorCode.SYSTEM_ERROR, "发送考勤月度统计消息失败");
        }
        /*
        List<AttendancePayrollSourceVO> stats = computePayrollSources(requestDTO.getMonth(), requestDTO.getEmployeeIds());
        stats.forEach(stat -> stringRedisTemplate.opsForValue().set(
                AttendanceCacheKeys.monthStat(stat.getEmployeeId(), requestDTO.getMonth()),
                JSONUtil.toJsonStr(stat),
                Duration.ofDays(35)));
        tempPublishMonthlyStatGenerateMessage(requestDTO);
        */
        return MonthlyStatGenerateVO.builder()
                .month(requestDTO.getMonth())
                .employeeCount(requestDTO.getEmployeeIds() == null ? 0 : requestDTO.getEmployeeIds().size())
                .success(true)
                .build();
    }

    /**
     * 处理月度统计生成消息。
     *
     * @param message 月度统计生成消息
     * 本方法使用的工具类: AttendanceCacheKeys(本模块cache包),JSONUtil(hutool),StringRedisTemplate(spring-data-redis)
     */
    public void handleMonthlyStatGenerateMessage(AttendanceMonthlyStatGenerateMessage message) {
        try {
            List<AttendancePayrollSourceVO> stats = computePayrollSources(message.getMonth(), message.getEmployeeIds());
            stats.forEach(stat -> stringRedisTemplate.opsForValue().set(
                    AttendanceCacheKeys.monthStat(stat.getEmployeeId(), message.getMonth()),
                    JSONUtil.toJsonStr(stat),
                    Duration.ofDays(35)));
        } finally {
            stringRedisTemplate.delete(AttendanceCacheKeys.monthStatGenerateLock(message.getMonth()));
        }
    }

    /**
     * 临时发布月度统计生成消息。
     *
     * @param requestDTO 生成请求
     * 本方法使用的工具类: JSONUtil(hutool)
     */
    //private void tempPublishMonthlyStatGenerateMessage(MonthlyStatGenerateRequestDTO requestDTO) {
    //    log.info("temp publish attendance.stat.generate: {}", JSONUtil.toJsonStr(requestDTO));
    //}

    /**
     * 获取薪资考勤数据。
     *
     * @param month    月份
     * @param employeeIds 员工ID
     * @return 薪资考勤数据

    /**
     * 查询薪资考勤数据（批量版：先查Redis缓存，未命中员工一次性批量查DB并回填缓存）。
     *
     * @param month       月份
     * @param employeeIds 员工ID列表
     * @return 薪资考勤数据
     * 本方法使用的工具类: JSONUtil(hutool),StringRedisTemplate(spring-data-redis),AttendanceCacheKeys(本模块cache包),HashMap(JDK)
     */
    @Override
    public List<AttendancePayrollSourceVO> getPayrollSource(String month, List<Long> employeeIds) {
        List<EmployeeSnapshotEntity> employees = findStatEmployees(employeeIds);
        // 1. 批量查 Redis 缓存
        Map<Long, AttendancePayrollSourceVO> cachedMap = new HashMap<>();
        List<EmployeeSnapshotEntity> uncachedEmployees = new ArrayList<>();
        for (EmployeeSnapshotEntity e : employees) {
            String cached = stringRedisTemplate.opsForValue().get(AttendanceCacheKeys.monthStat(e.getId(), month));
            if (StrUtil.isNotBlank(cached)) {
                cachedMap.put(e.getId(), JSONUtil.toBean(cached, AttendancePayrollSourceVO.class));
            } else {
                uncachedEmployees.add(e);
            }
        }
        // 2. 对未缓存的员工批量查DB并回填缓存
        if (!uncachedEmployees.isEmpty()) {
            List<AttendancePayrollSourceVO> computed = computePayrollSourcesBatch(month, uncachedEmployees);
            for (AttendancePayrollSourceVO vo : computed) {
                cachedMap.put(vo.getEmployeeId(), vo);
                stringRedisTemplate.opsForValue().set(
                        AttendanceCacheKeys.monthStat(vo.getEmployeeId(), month),
                        JSONUtil.toJsonStr(vo),
                        30, TimeUnit.MINUTES);
            }
        }
        // 3. 按原始顺序组装结果
        return employees.stream()
                .map(e -> cachedMap.get(e.getId()))
                .toList();
    }

    /**
    /**
     * 批量计算薪资考勤数据（月度统计生成等场景使用）。
     *
     * @param month       月份
     * @param employeeIds 员工ID列表
     * @return 薪资考勤数据
     * 本方法使用的工具类: List(JDK)
     */
    private List<AttendancePayrollSourceVO> computePayrollSources(String month, List<Long> employeeIds) {
        return computePayrollSourcesBatch(month, findStatEmployees(employeeIds));
    }

    /**
     * 查询统计员工范围。
     *
     * @param employeeIds 员工ID列表
     * @return 员工快照列表
     * 本方法使用的工具类: Collections(JDK)
     */
    private List<EmployeeSnapshotEntity> findStatEmployees(List<Long> employeeIds) {
        if (employeeIds != null && !employeeIds.isEmpty()) {
            return employeeSnapshotMapper.selectList(new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                    .in(EmployeeSnapshotEntity::getId, employeeIds));
        }
        List<EmployeeSnapshotEntity> employees = employeeSnapshotMapper.selectList(new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                .ne(EmployeeSnapshotEntity::getEmploymentStatus, 4));
        return employees == null ? Collections.emptyList() : employees;
    }

    /**
     * 计算单个员工薪资考勤数据。
     *
     * @param month    月份
     * @param employee 员工快照
     * @return 薪资考勤数据
     * 本方法使用的工具类: YearMonth(JDK),BigDecimal(JDK)
     */
    private AttendancePayrollSourceVO computePayrollSource(String month, EmployeeSnapshotEntity employee) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<AttendanceRecordEntity> records = attendanceRecordMapper.selectByEmployeeAndDateRange(employee.getId(), startDate, endDate);
        BigDecimal leaveDays = leaveRequestMapper.selectList(new LambdaQueryWrapper<LeaveRequestEntity>()
                        .eq(LeaveRequestEntity::getEmployeeId, employee.getId())
                        .eq(LeaveRequestEntity::getApprovalStatus, 2)
                        .ge(LeaveRequestEntity::getStartTime, startDate.atStartOfDay())
                        .le(LeaveRequestEntity::getStartTime, endDate.atTime(LocalTime.MAX)))
                .stream()
                .map(LeaveRequestEntity::getTotalDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int shouldAttendDays = countWeekdays(startDate, endDate);
        int lateCount = (int) records.stream().filter(record -> "LATE".equals(record.getClockInStatus())).count();
        int earlyLeaveCount = (int) records.stream().filter(record -> "EARLY_LEAVE".equals(record.getClockOutStatus())).count();
        int actualAttendDays = (int) records.stream()
                .filter(record -> record.getClockInTime() != null || record.getClockOutTime() != null)
                .count();
        return AttendancePayrollSourceVO.builder()
                .employeeId(employee.getId())
                .employeeNo(employee.getEmployeeNo())
                .employeeName(employee.getEmployeeName())
                .shouldAttendDays(shouldAttendDays)
                .actualAttendDays(actualAttendDays)
                .lateCount(lateCount)
                .earlyLeaveCount(earlyLeaveCount)
                .leaveDays(leaveDays)
                .absenceDays(BigDecimal.valueOf(Math.max(0, shouldAttendDays - actualAttendDays)).subtract(leaveDays).max(BigDecimal.ZERO))
                .overtimeHours(BigDecimal.ZERO)
                .build();
    }

    /**
     * 批量计算薪资考勤数据：一次性查出所有员工的打卡记录和请假数据，内存分组后逐员工聚合。
     *
     * @param month     月份
     * @param employees 待计算的员工快照列表
     * @return 薪资考勤数据
     * 本方法使用的工具类: YearMonth(JDK),BigDecimal(JDK),Collectors(JDK),Stream(JDK) 
     */
    private List<AttendancePayrollSourceVO> computePayrollSourcesBatch(String month, List<EmployeeSnapshotEntity> employees) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<Long> employeeIds = employees.stream().map(EmployeeSnapshotEntity::getId).toList();

        // 批量查打卡记录
        List<AttendanceRecordEntity> allRecords = attendanceRecordMapper.selectByEmployeeIdsAndDateRange(
                employeeIds, startDate, endDate);
        Map<Long, List<AttendanceRecordEntity>> recordMap = allRecords.stream()
                .collect(Collectors.groupingBy(AttendanceRecordEntity::getEmployeeId));

        // 批量查请假记录
        List<LeaveRequestEntity> allLeaves = leaveRequestMapper.selectList(
                new LambdaQueryWrapper<LeaveRequestEntity>()
                        .in(LeaveRequestEntity::getEmployeeId, employeeIds)
                        .eq(LeaveRequestEntity::getApprovalStatus, 2)
                        .ge(LeaveRequestEntity::getStartTime, startDate.atStartOfDay())
                        .le(LeaveRequestEntity::getStartTime, endDate.atTime(LocalTime.MAX)));
        Map<Long, BigDecimal> leaveDayMap = allLeaves.stream()
                .collect(Collectors.groupingBy(LeaveRequestEntity::getEmployeeId,
                        Collectors.reducing(BigDecimal.ZERO, LeaveRequestEntity::getTotalDays, BigDecimal::add)));

        int shouldAttendDays = countWeekdays(startDate, endDate);

        return employees.stream().map(employee -> {
            List<AttendanceRecordEntity> records = recordMap.getOrDefault(employee.getId(), List.of());
            BigDecimal leaveDays = leaveDayMap.getOrDefault(employee.getId(), BigDecimal.ZERO);

            int lateCount = (int) records.stream().filter(r -> "LATE".equals(r.getClockInStatus())).count();
            int earlyLeaveCount = (int) records.stream().filter(r -> "EARLY_LEAVE".equals(r.getClockOutStatus())).count();
            int actualAttendDays = (int) records.stream()
                    .filter(r -> r.getClockInTime() != null || r.getClockOutTime() != null)
                    .count();

            return AttendancePayrollSourceVO.builder()
                    .employeeId(employee.getId())
                    .employeeNo(employee.getEmployeeNo())
                    .employeeName(employee.getEmployeeName())
                    .shouldAttendDays(shouldAttendDays)
                    .actualAttendDays(actualAttendDays)
                    .lateCount(lateCount)
                    .earlyLeaveCount(earlyLeaveCount)
                    .leaveDays(leaveDays)
                    .absenceDays(BigDecimal.valueOf(Math.max(0, shouldAttendDays - actualAttendDays))
                            .subtract(leaveDays).max(BigDecimal.ZERO))
                    .overtimeHours(BigDecimal.ZERO)
                    .build();
        }).toList();
    }

    /**
     * 计算工作日数量。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 工作日数量
     * 本方法使用的工具类: IntStream(JDK)
     */
    private int countWeekdays(LocalDate startDate, LocalDate endDate) {
        return attendanceCalendarConfigService.countWorkdays(startDate, endDate);
    }

    /**
     * 解析请假类型。
     *
     * @param requestDTO 请假申请请求
     * @return 请假类型值
     * 本方法使用的工具类: StrUtil(hutool),GlobalException(hrms-common)
     */
    private String resolveLeaveType(LeaveCreateRequestDTO requestDTO) {
        if (StrUtil.isNotBlank(requestDTO.getLeaveType())) {
            return requestDTO.getLeaveType();
        }
        if (requestDTO.getLeaveTypeId() == null) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "请假类型不能为空");
        }
        DictDataEntity dict = dictDataMapper.selectById(requestDTO.getLeaveTypeId());
        if (dict == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "请假类型不存在");
        }
        return dict.getDictValue();
    }

    /**
     * 计算请假天数。
     *
     * @param requestDTO 请假申请请求
     * @return 请假天数
     * 本方法使用的工具类: ChronoUnit(JDK),BigDecimal(JDK)
     */
    private BigDecimal calculateLeaveDays(LeaveCreateRequestDTO requestDTO) {
        long days = ChronoUnit.DAYS.between(requestDTO.getStartDate(), requestDTO.getEndDate()) + 1;
        BigDecimal total = BigDecimal.valueOf(days);
        if ("PM".equalsIgnoreCase(requestDTO.getStartPeriod())) {
            total = total.subtract(BigDecimal.valueOf(0.5));
        }
        if ("AM".equalsIgnoreCase(requestDTO.getEndPeriod())) {
            total = total.subtract(BigDecimal.valueOf(0.5));
        }
        return total;
    }

    /**
     * 将日期和时段转换为时间。
     *
     * @param date   日期
     * @param period 时段
     * @param start  是否开始时间
     * @return 日期时间
     * 本方法使用的工具类: 无
     */
    private LocalDateTime toPeriodTime(LocalDate date, String period, boolean start) {
        if ("PM".equalsIgnoreCase(period)) {
            return date.atTime(start ? LocalTime.NOON : LocalTime.MAX);
        }
        return date.atTime(start ? LocalTime.MIN : LocalTime.NOON);
    }

    /**
     * 解析附件地址。
     *
     * @param requestDTO 请假申请请求
     * @return 附件地址
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private String resolveAttachment(LeaveCreateRequestDTO requestDTO) {
        if (StrUtil.isNotBlank(requestDTO.getAttachment())) {
            return requestDTO.getAttachment();
        }
        return requestDTO.getAttachmentFileId() == null ? null : String.valueOf(requestDTO.getAttachmentFileId());
    }

    /**
     * 临时发起请假审批。
     *
     * @param employeeId 员工ID
     * @param requestDTO 请假申请请求
     * @return 审批实例ID
     * 本方法使用的工具类: IdUtil(hutool)
     */
    //private Long tempStartLeaveApproval(Long employeeId, LeaveCreateRequestDTO requestDTO) {
    //    return IdUtil.getSnowflakeNextId();
    //}

    /**
     * 构建请假创建结果。
     *
     * @param entity 请假申请
     * @return 创建结果
     * 本方法使用的工具类: 无
     */
    private LeaveCreateVO buildLeaveCreateVO(LeaveRequestEntity entity) {
        return LeaveCreateVO.builder()
                .id(entity.getId())
                .approvalInstanceId(entity.getApprovalInstanceId())
                .approvalStatus(entity.getApprovalStatus())
                .build();
    }

    /**
     * 转换请假类型视图。
     *
     * @param entity 字典数据
     * @return 请假类型视图
     * 本方法使用的工具类: 无
     */
    private LeaveTypeVO toLeaveTypeVO(DictDataEntity entity) {
        return LeaveTypeVO.builder()
                .id(entity.getId())
                .label(entity.getDictLabel())
                .value(entity.getDictValue())
                .build();
    }

    /**
     * 校验补卡申请是否重复。
     *
     * @param employeeId 员工ID
     * @param date       补卡日期
     * @param period     补卡类型
     * 本方法使用的工具类: GlobalException(hrms-common)
     */
    private void checkCorrectionDuplicate(Long employeeId, LocalDate date, ClockPeriodEnum period) {
        Long count = attendanceCorrectionMapper.selectCount(new LambdaQueryWrapper<AttendanceCorrectionEntity>()
                .eq(AttendanceCorrectionEntity::getEmployeeId, employeeId)
                .eq(AttendanceCorrectionEntity::getCorrectionDate, date)
                .eq(AttendanceCorrectionEntity::getCorrectionType, period.name())
                .eq(AttendanceCorrectionEntity::getApprovalStatus, 1));
        if (count != null && count > 0) {
            throw new GlobalException(ATTENDANCE_CORRECTION_DUPLICATE);
        }
    }

    /**
     * 获取或创建补卡关联的打卡记录。
     *
     * @param employeeId 员工ID
     * @param date       补卡日期
     * @return 打卡记录
     * 本方法使用的工具类: 无
     */
    private AttendanceRecordEntity getOrCreateCorrectionRecord(Long employeeId, LocalDate date) {
        AttendanceRecordEntity existing = attendanceRecordMapper.selectByEmployeeAndDate(employeeId, date);
        if (existing != null) {
            return existing;
        }
        AttendanceGroupEntity group = resolveEmployeeAttendanceGroup(employeeId, date);
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setEmployeeId(employeeId);
        record.setGroupId(group.getId());
        record.setRecordDate(date);
        record.setCorrectionStatus("PENDING");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        attendanceRecordMapper.insertClockIn(record);
        return record;
    }

    /**
     * 临时发起补卡审批。
     *
     * @param employeeId  员工ID
     * @param recordId    打卡记录ID
     * @param requestDTO  补卡申请请求
     * @return 审批实例ID
     * 本方法使用的工具类: IdUtil(hutool)
     */
    //private Long tempStartCorrectionApproval(Long employeeId, Long recordId, AttendanceCorrectionCreateRequestDTO requestDTO) {
    //    return IdUtil.getSnowflakeNextId();
    //}

    /**
     * 构建补卡创建响应。
     *
     * @param correction 补卡申请
     * @return 创建响应
     * 本方法使用的工具类: 无
     */
    private AttendanceCorrectionCreateVO buildCorrectionCreateVO(AttendanceCorrectionEntity correction) {
        return AttendanceCorrectionCreateVO.builder()
                .id(correction.getId())
                .recordId(correction.getRecordId())
                .approvalInstanceId(correction.getApprovalInstanceId())
                .approvalStatus(correction.getApprovalStatus())
                .build();
    }

    /**
     * 删除个人月历缓存
     * @param employeeId 员工ID
     * @param date       日期
     * 本方法使用的工具类: AttendanceCacheKeys(本模块cache包),StringRedisTemplate(spring-data-redis)
     */
    private void evictCalendarCache(Long employeeId, LocalDate date) {
        stringRedisTemplate.delete(AttendanceCacheKeys.monthCalendar(employeeId, YearMonth.from(date).toString()));
    }

    /**
     * 从数据库构建个人月度打卡日历。
     *
     * @param employeeId  员工ID
     * @param parsedMonth 月份
     * @return 月度日历
     * 本方法使用的工具类: Collectors(JDK),IntStream(JDK)
     */
    private AttendanceCalendarVO buildCalendarFromDatabase(Long employeeId, YearMonth parsedMonth) {
        LocalDate startDate = parsedMonth.atDay(1);
        LocalDate endDate = parsedMonth.atEndOfMonth();
        Map<LocalDate, AttendanceRecordEntity> recordMap = attendanceRecordMapper
                .selectByEmployeeAndDateRange(employeeId, startDate, endDate)
                .stream()
                .collect(Collectors.toMap(AttendanceRecordEntity::getRecordDate, record -> record));
        Set<LocalDate> leaveDates = findApprovedLeaveDates(employeeId, startDate, endDate);
        List<AttendanceCalendarDayVO> days = IntStream.rangeClosed(1, parsedMonth.lengthOfMonth())
                .mapToObj(day -> buildCalendarDay(parsedMonth.atDay(day), recordMap.get(parsedMonth.atDay(day)), leaveDates))
                .toList();
        return AttendanceCalendarVO.builder()
                .employeeId(employeeId)
                .yearMonth(parsedMonth.toString())
                .days(days)
                .build();
    }

    /**
     * 查询审批通过的请假日期集合。
     *
     * @param employeeId 员工ID
     * @param startDate  月初
     * @param endDate    月末
     * @return 请假日期集合
     * 本方法使用的工具类: Collectors(JDK)
     */
    private Set<LocalDate> findApprovedLeaveDates(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<LeaveRequestEntity> leaves = leaveRequestMapper.selectList(new LambdaQueryWrapper<LeaveRequestEntity>()
                .eq(LeaveRequestEntity::getEmployeeId, employeeId)
                .eq(LeaveRequestEntity::getApprovalStatus, 2)
                .le(LeaveRequestEntity::getStartTime, endDate.atTime(LocalTime.MAX))
                .ge(LeaveRequestEntity::getEndTime, startDate.atStartOfDay()));
        return leaves.stream()
                .flatMap(leave -> leave.getStartTime().toLocalDate()
                        .datesUntil(leave.getEndTime().toLocalDate().plusDays(1)))
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .collect(Collectors.toSet());
    }

    /**
     * 构建单日打卡日历。
     *
     * @param date       日期
     * @param record     打卡记录
     * @param leaveDates 请假日期集合
     * @return 单日打卡日历
     * 本方法使用的工具类: 无
     */
    private AttendanceCalendarDayVO buildCalendarDay(LocalDate date, AttendanceRecordEntity record, Set<LocalDate> leaveDates) {
        AttendanceCalendarDayVO day = AttendanceCalendarDayVO.builder()
                .date(date)
                .leave(leaveDates.contains(date))
                .clockInTime(record == null ? null : record.getClockInTime())
                .clockOutTime(record == null ? null : record.getClockOutTime())
                .clockInIp(record == null ? null : record.getClockInIp())
                .clockOutIp(record == null ? null : record.getClockOutIp())
                .clockInGps(record == null ? null : record.getClockInGps())
                .clockOutGps(record == null ? null : record.getClockOutGps())
                .clockInStatus(record == null ? null : record.getClockInStatus())
                .clockOutStatus(record == null ? null : record.getClockOutStatus())
                .build();
        return AttendanceCalendarDayVO.builder()
                .date(day.getDate())
                .leave(day.getLeave())
                .clockInTime(day.getClockInTime())
                .clockOutTime(day.getClockOutTime())
                .clockInIp(day.getClockInIp())
                .clockOutIp(day.getClockOutIp())
                .clockInGps(day.getClockInGps())
                .clockOutGps(day.getClockOutGps())
                .clockInStatus(day.getClockInStatus())
                .clockOutStatus(day.getClockOutStatus())
                .dayStatus(resolveDayStatus(day))
                .build();
    }

    /**
     * 计算单日综合状态。
     *
     * @param day 单日打卡日历
     * @return 综合状态
     * 本方法使用的工具类: 无
     */
    private String resolveDayStatus(AttendanceCalendarDayVO day) {
        if (Boolean.TRUE.equals(day.getLeave())) {
            return "LEAVE";
        }
        if ("LATE".equals(day.getClockInStatus())) {
            return "LATE";
        }
        if ("EARLY_LEAVE".equals(day.getClockOutStatus())) {
            return "EARLY_LEAVE";
        }
        if (day.getClockInTime() != null || day.getClockOutTime() != null) {
            return "NORMAL";
        }
        return "NONE";
    }

    /**
     * 查询当前登录用户关联的员工快照。
     *
     * @return 员工快照
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),GlobalException(hrms-common)
     */
    private EmployeeSnapshotEntity getCurrentEmployeeSnapshot() {
        Long userId = SecurityContextHolder.getUserId();
        EmployeeSnapshotEntity employee = employeeSnapshotMapper.selectOne(new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                .eq(EmployeeSnapshotEntity::getUserId, userId)
                .last("LIMIT 1"));
        if (employee == null) {
            throw new GlobalException(ATTENDANCE_EMPLOYEE_NOT_FOUND);
        }
        return employee;
    }

    /**
     * 解析员工在指定日期生效的考勤组。
     *
     * @param employeeId  员工ID
     * @param targetDate 目标日期
     * @return 考勤组
     * 本方法使用的工具类: GlobalException(hrms-common),LambdaQueryWrapper(mybatis-plus)
     */
    private AttendanceGroupEntity resolveEmployeeAttendanceGroup(Long employeeId, LocalDate targetDate) {
        AttendanceGroupMemberEntity member = attendanceGroupMemberMapper.selectOne(new LambdaQueryWrapper<AttendanceGroupMemberEntity>()
                .eq(AttendanceGroupMemberEntity::getEmployeeId, employeeId)
                .eq(AttendanceGroupMemberEntity::getStatus, 1)
                .eq(AttendanceGroupMemberEntity::getIsDeleted, 0)
                .le(AttendanceGroupMemberEntity::getEffectiveStartDate, targetDate)
                .and(wrapper -> wrapper.isNull(AttendanceGroupMemberEntity::getEffectiveEndDate)
                        .or()
                        .ge(AttendanceGroupMemberEntity::getEffectiveEndDate, targetDate))
                .orderByDesc(AttendanceGroupMemberEntity::getEffectiveStartDate)
                .orderByDesc(AttendanceGroupMemberEntity::getId)
                .last("LIMIT 1"));
        if (member == null) {
            throw new GlobalException(ATTENDANCE_GROUP_NOT_FOUND, "当前员工未配置有效考勤组");
        }
        AttendanceGroupEntity group = attendanceGroupMapper.selectOne(new LambdaQueryWrapper<AttendanceGroupEntity>()
                .eq(AttendanceGroupEntity::getId, member.getGroupId())
                .eq(AttendanceGroupEntity::getStatus, 1)
                .eq(AttendanceGroupEntity::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (group == null) {
            throw new GlobalException(ATTENDANCE_GROUP_NOT_FOUND, "当前员工未配置有效考勤组");
        }
        return group;
    }

    /**
     * 解析员工当前生效考勤组，未配置时返回空。
     *
     * @param employeeId  员工ID
     * @param targetDate 目标日期
     * @return 当前生效考勤组，为空表示未配置
     * 本方法使用的工具类: 无
     */
    private AttendanceGroupEntity resolveCurrentAttendanceGroupOrNull(Long employeeId, LocalDate targetDate) {
        try {
            return resolveEmployeeAttendanceGroup(employeeId, targetDate);
        } catch (GlobalException ex) {
            if (ATTENDANCE_GROUP_NOT_FOUND.getCode() == ex.getErrorCode().getCode()) {
                return null;
            }
            throw ex;
        }
    }

//    /**
//     * 已停用：临时解析员工所属考勤组，仅作历史临时逻辑参考；当前已替换为 hr_attendance_group_member 生效关系表解析。
//     *
//     * @param employee 员工快照
//     * @return 考勤组
//     * 本方法使用的工具类: GlobalException(hrms-common)
//     */
//    private AttendanceGroupEntity tempResolveEmployeeAttendanceGroup(EmployeeSnapshotEntity employee) {
//        // attendanceGroupMemberService.getEmployeeGroup(employee.getId()); 本接口需要调用考勤组成员/员工归属解析接口。
//        AttendanceGroupEntity group = attendanceGroupMapper.selectOne(new LambdaQueryWrapper<AttendanceGroupEntity>()
//                .eq(AttendanceGroupEntity::getStatus, 1)
//                .orderByAsc(AttendanceGroupEntity::getId)
//                .last("LIMIT 1"));
//        if (group == null) {
//            throw new GlobalException(ATTENDANCE_GROUP_NOT_FOUND);
//        }
//        return group;
//    }

    /**
     * 校验打卡 IP 与 GPS 范围。
     *
     * @param group      考勤组
     * @param requestDTO 打卡请求
     * @param clientIp   客户端 IP
     * 本方法使用的工具类: StrUtil(hutool),JSONUtil(hutool),GlobalException(hrms-common)
     */
    private void validateClockRange(AttendanceGroupEntity group, AttendanceClockRequestDTO requestDTO, String clientIp) {
        if (StrUtil.isNotBlank(group.getClockIpWhitelist())) {
            boolean ipAllowed = Arrays.stream(group.getClockIpWhitelist().split(","))
                    .map(String::trim)
                    .anyMatch(allowedIp -> allowedIp.equals(clientIp));
            if (!ipAllowed) {
                throw new GlobalException(ATTENDANCE_CLOCK_RANGE_INVALID);
            }
        }
        if (StrUtil.isNotBlank(group.getClockGpsScope()) && requestDTO.getLatitude() != null && requestDTO.getLongitude() != null) {
            if (!isGpsAllowed(group.getClockGpsScope(), requestDTO.getLatitude(), requestDTO.getLongitude())) {
                throw new GlobalException(ATTENDANCE_CLOCK_RANGE_INVALID);
            }
        }
    }

    /**
     * 判断 GPS 是否在考勤组范围内。
     *
     * @param gpsScope  范围配置
     * @param latitude  纬度
     * @param longitude 经度
     * @return 是否允许打卡
     * 本方法使用的工具类: JSONUtil(hutool)
     */
    private boolean isGpsAllowed(String gpsScope, BigDecimal latitude, BigDecimal longitude) {
        try {
            // 解析 JSON 配置
            JSONObject json = JSONUtil.parseObj(gpsScope);
            // 获取中心点纬度和经度
            double centerLat = json.getDouble("latitude", json.getDouble("lat", 0D));
            double centerLng = json.getDouble("longitude", json.getDouble("lng", 0D));
            // 获取半径
            double radius = json.getDouble("radius", 0D);
            if (radius <= 0) {
                return true;
            }
            return distanceMeters(centerLat, centerLng, latitude.doubleValue(), longitude.doubleValue()) <= radius;
        } catch (Exception ex) {
            log.warn("parse attendance gps scope failed, scope={}", gpsScope, ex);
            return true;
        }
    }

    /**
     * 计算两个经纬度点之间的近似距离。
     *
     * @param lat1 第一个点纬度
     * @param lng1 第一个点经度
     * @param lat2 第二个点纬度
     * @param lng2 第二个点经度
     * @return 距离，单位米
     * 本方法使用的工具类: Math(JDK)
     */
    private double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000D;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * 解析打卡时段。
     *
     * @param requestDTO 打卡请求
     * @param existing   当天已有记录
     * @return 打卡时段
     * 本方法使用的工具类: ClockPeriodEnum(本模块enums包),GlobalException(hrms-common)
     */
    private ClockPeriodEnum resolveClockPeriod(AttendanceClockRequestDTO requestDTO, AttendanceRecordEntity existing) {
        ClockPeriodEnum period = ClockPeriodEnum.parse(requestDTO.getType());
        if (period != null) {
            return period;
        }
        if (existing == null || existing.getClockInTime() == null) {
            return ClockPeriodEnum.CLOCK_IN;
        }
        if (existing.getClockOutTime() == null) {
            return ClockPeriodEnum.CLOCK_OUT;
        }
        throw new GlobalException(ATTENDANCE_CLOCK_DUPLICATE);
    }

    /**
     * 计算打卡状态。
     *
     * @param group     考勤组
     * @param period    打卡时段
     * @param clockTime 打卡时间
     * @return 打卡状态
     * 本方法使用的工具类: 无
     */
    private String calculateClockStatus(AttendanceGroupEntity group, ClockPeriodEnum period, LocalTime clockTime) {
        // 如果是打卡，则判断是否迟到
        if (ClockPeriodEnum.CLOCK_IN.equals(period)) {
            LocalTime lateLine = group.getWorkStartTime().plusMinutes(group.getLateThresholdMinutes());
            return clockTime.isAfter(lateLine) ? "LATE" : "NORMAL";
        }
        // 如果是退卡，则判断是否早退
        LocalTime earlyLine = group.getWorkEndTime().minusMinutes(group.getEarlyLeaveThresholdMinutes());
        return clockTime.isBefore(earlyLine) ? "EARLY_LEAVE" : "NORMAL";
    }

    /**
     * 构建打卡记录。
     *
     * @param employeeId 员工ID
     * @param groupId    考勤组ID
     * @param recordDate 打卡日期
     * @param now        当前时间
     * @param period     打卡时段
     * @param status     打卡状态
     * @param requestDTO 打卡请求
     * @param clientIp   客户端 IP
     * @return 打卡记录
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private AttendanceRecordEntity buildClockRecord(Long employeeId, Long groupId, LocalDate recordDate, LocalDateTime now,
                                                    ClockPeriodEnum period, String status, AttendanceClockRequestDTO requestDTO,
                                                    String clientIp) {
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setEmployeeId(employeeId);
        record.setGroupId(groupId);
        record.setRecordDate(recordDate);
        record.setDeviceInfo(requestDTO.getDeviceInfo());
        record.setCorrectionStatus("NONE");
        record.setCreateTime(now);
        record.setUpdateTime(now);
        String gps = buildGps(requestDTO);
        if (ClockPeriodEnum.CLOCK_IN.equals(period)) {
            record.setClockInTime(now);
            record.setClockInStatus(status);
            record.setClockInIp(clientIp);
            record.setClockInGps(gps);
        } else {
            record.setClockOutTime(now);
            record.setClockOutStatus(status);
            record.setClockOutIp(clientIp);
            record.setClockOutGps(gps);
        }
        return record;
    }

    /**
     * 构建 GPS 字符串。
     *
     * @param requestDTO 打卡请求
     * @return GPS 字符串
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private String buildGps(AttendanceClockRequestDTO requestDTO) {
        if (requestDTO.getLatitude() == null || requestDTO.getLongitude() == null) {
            return null;
        }
        return StrUtil.format("{},{}", requestDTO.getLatitude(), requestDTO.getLongitude());
    }

    /**
     * 持久化打卡记录。
     *
     * @param existing 已有记录
     * @param record   新记录
     * @param period   打卡时段
     * 本方法使用的工具类: GlobalException(hrms-common)
     */
    private void persistClockRecord(AttendanceRecordEntity existing, AttendanceRecordEntity record, ClockPeriodEnum period) {
        try {
            if (existing == null) {
                if (ClockPeriodEnum.CLOCK_IN.equals(period)) {
                    attendanceRecordMapper.insertClockIn(record);
                } else {
                    attendanceRecordMapper.insertClockOut(record);
                }
                return;
            }
            record.setId(existing.getId());
            // 更新记录
            int updated = ClockPeriodEnum.CLOCK_IN.equals(period)
                    ? attendanceRecordMapper.updateClockIn(record)
                    : attendanceRecordMapper.updateClockOut(record);
            // 如果更新行数为0，则抛出重复打卡异常
            if (updated == 0) {
                throw new GlobalException(ATTENDANCE_CLOCK_DUPLICATE);
            }
            // 如果更新行数为0，则抛出重复打卡异常
        } catch (DuplicateKeyException ex) {
            throw new GlobalException(ATTENDANCE_CLOCK_DUPLICATE);
        }
    }

    /**
     * 构建打卡成功事件。
     *
     * @param record     打卡记录
     * @param period     打卡时段
     * @param status     打卡状态
     * @param clockTime  打卡时间
     * @param deviceInfo 设备信息
     * @return 打卡成功事件
     * 本方法使用的工具类: IdUtil(hutool)
     */
    private AttendanceClockCreatedEvent buildClockCreatedEvent(AttendanceRecordEntity record, ClockPeriodEnum period,
                                                               String status, LocalDateTime clockTime, String deviceInfo) {
        return AttendanceClockCreatedEvent.builder()
                .messageId(IdUtil.fastSimpleUUID())
                .employeeId(record.getEmployeeId())
                .groupId(record.getGroupId())
                .recordId(record.getId())
                .recordDate(record.getRecordDate())
                .period(period.name())
                .status(status)
                .clockTime(clockTime)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * 发布打卡成功事件。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: AttendanceClockCreatedProducer(本模块mq包)
     */
    private void publishClockCreatedEvent(AttendanceClockCreatedEvent event) {
        try {
            attendanceClockCreatedProducer.send(event);
        } catch (Exception ex) {
            log.warn("publish attendance.clock.created failed, event={}", event, ex);
            // tempPublishClockCreatedEvent(event); 旧同步兜底逻辑先保留注释，当前打卡后置动作改为 RabbitMQ 异步消费。
        }
    }

    /**
     * 临时处理打卡成功事件。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: AttendanceClockEventHandler(本模块mq包)
     */
    //private void tempPublishClockCreatedEvent(AttendanceClockCreatedEvent event) {
    //    try {
    //        attendanceClockEventHandler.handleClockCreatedEvent(event);
    //    } catch (Exception ex) {
    //        log.warn("temp handle attendance.clock.created failed, event={}", event, ex);
    //    }
    //}

    /**
     * 构建打卡响应。
     *
     * @param record    打卡记录
     * @param period    打卡时段
     * @param status    打卡状态
     * @param clockTime 打卡时间
     * @return 打卡响应
     * 本方法使用的工具类: 无
     */
    private AttendanceClockVO buildClockVO(AttendanceRecordEntity record, ClockPeriodEnum period, String status, LocalDateTime clockTime) {
        String clientIp = ClockPeriodEnum.CLOCK_IN.equals(period) ? record.getClockInIp() : record.getClockOutIp();
        String clockGps = ClockPeriodEnum.CLOCK_IN.equals(period) ? record.getClockInGps() : record.getClockOutGps();
        return AttendanceClockVO.builder()
                .recordId(record.getId())
                .employeeId(record.getEmployeeId())
                .groupId(record.getGroupId())
                .recordDate(record.getRecordDate())
                .period(period.name())
                .status(status)
                .clockTime(clockTime)
                .clockGps(clockGps)
                .clientIp(clientIp)
                .build();
    }

    /**
     * 填充考勤组适用范围存储字段。
     *
     * @param entity      考勤组实体
     * @param memberRange 适用范围请求
     * 本方法使用的工具类: JSONUtil(hutool),GlobalException(hrms-common)
     */
    private void fillGroupScope(AttendanceGroupEntity entity, AttendanceGroupMemberRangeDTO memberRange) {
        if (memberRange == null || StrUtil.isBlank(memberRange.getScopeType())) {
            entity.setScopeType(null);
            entity.setScopeValue(null);
            return;
        }
        String scopeType = memberRange.getScopeType().trim().toUpperCase();
        JSONObject scopeValue = new JSONObject();
        // 根据适用范围类型进行不同的处理
        switch (scopeType) {
            case GROUP_SCOPE_DEPT -> {
                List<Long> deptIds = normalizeIds(memberRange.getDeptIds());
                if (deptIds.isEmpty()) {
                    throw new GlobalException(ErrorCode.PARAM_REQUIRED, "请选择适用部门");
                }
                scopeValue.set("deptIds", deptIds);
            }
            case GROUP_SCOPE_POST -> {
                if (memberRange.getPostId() == null) {
                    throw new GlobalException(ErrorCode.PARAM_REQUIRED, "请选择适用职位");
                }
                scopeValue.set("postId", memberRange.getPostId());
            }
            case GROUP_SCOPE_EMPLOYEE -> {
                if (memberRange.getDeptId() == null) {
                    throw new GlobalException(ErrorCode.PARAM_REQUIRED, "请选择员工所属部门");
                }
                List<Long> employeeIds = normalizeIds(memberRange.getEmployeeIds());
                scopeValue.set("deptId", memberRange.getDeptId());
                scopeValue.set("employeeIds", employeeIds);
            }
            default -> throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "适用范围类型不正确");
        }
        entity.setScopeType(scopeType);
        entity.setScopeValue(JSONUtil.toJsonStr(scopeValue));
    }

    /**
     * 重建考勤组成员关系。
     *
     * @param groupId     考勤组ID
     * @param memberRange 适用范围请求
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),GlobalException(hrms-common)
     */
    private void rebuildGroupMembers(Long groupId, AttendanceGroupMemberRangeDTO memberRange) {
        if (groupId == null) {
            return;
        }
        logicDeleteGroupMembers(groupId);
        if (memberRange == null || StrUtil.isBlank(memberRange.getScopeType())) {
            return;
        }
        List<Long> employeeIds = resolveGroupMemberEmployeeIds(memberRange);
        if (employeeIds.isEmpty()) {
            return;
        }
        insertGroupMembers(groupId, employeeIds);
    }

    /**
     * 校验新建考勤组的成员范围。
     *
     * @param memberRange 适用范围请求
     * 本方法使用的工具类: StrUtil(hutool),GlobalException(hrms-common)
     */
    private void validateCreateMemberRange(AttendanceGroupMemberRangeDTO memberRange) {
        if (memberRange == null || StrUtil.isBlank(memberRange.getScopeType())) {
            return;
        }
        String scopeType = memberRange.getScopeType().trim().toUpperCase();
        if (GROUP_SCOPE_EMPLOYEE.equals(scopeType) && normalizeIds(memberRange.getEmployeeIds()).isEmpty()) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "请选择指定员工");
        }
    }

    /**
     * 解析适用范围内的员工ID。
     *
     * @param memberRange 适用范围请求
     * @return 员工ID列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),GlobalException(hrms-common)
     */
    private List<Long> resolveGroupMemberEmployeeIds(AttendanceGroupMemberRangeDTO memberRange) {
        String scopeType = memberRange.getScopeType().trim().toUpperCase();
        return switch (scopeType) {
            case GROUP_SCOPE_DEPT -> listEmployeesByDeptIds(normalizeIds(memberRange.getDeptIds())).stream()
                    .map(EmployeeSnapshotEntity::getId)
                    .distinct()
                    .toList();
            case GROUP_SCOPE_POST -> listEmployeesByPostId(memberRange.getPostId()).stream()
                    .map(EmployeeSnapshotEntity::getId)
                    .distinct()
                    .toList();
            case GROUP_SCOPE_EMPLOYEE -> listEmployeesByIds(normalizeIds(memberRange.getEmployeeIds())).stream()
                    .filter(employee -> memberRange.getDeptId().equals(employee.getDeptId()))
                    .map(EmployeeSnapshotEntity::getId)
                    .distinct()
                    .toList();
            default -> throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "适用范围类型不正确");
        };
    }

    /**
     * 填充考勤组适用范围展示字段。
     *
     * @param vo 考勤组分页响应
     * @return 填充后的考勤组分页响应
     * 本方法使用的工具类: JSONUtil(hutool),Collectors(JDK)
     */
    private AttendanceGroupPageVO fillAttendanceGroupScope(AttendanceGroupPageVO vo) {
        if (vo == null) {
            return null;
        }
        JSONObject scopeValue = parseScopeValue(vo.getScopeValue());
        if (GROUP_SCOPE_DEPT.equalsIgnoreCase(vo.getScopeType())) {
            List<Long> deptIds = getLongList(scopeValue, "deptIds");
            vo.setDeptIds(deptIds);
            vo.setScopeName(buildDeptScopeName(deptIds));
        } else if (GROUP_SCOPE_POST.equalsIgnoreCase(vo.getScopeType())) {
            Long postId = scopeValue.getLong("postId");
            vo.setPostId(postId);
            vo.setScopeName(resolvePostName(postId));
        } else if (GROUP_SCOPE_EMPLOYEE.equalsIgnoreCase(vo.getScopeType())) {
            Long deptId = scopeValue.getLong("deptId");
            List<Long> employeeIds = getLongList(scopeValue, "employeeIds");
            vo.setDeptId(deptId);
            vo.setEmployeeIds(employeeIds);
            vo.setScopeName(buildEmployeeScopeName(deptId, employeeIds));
        } else {
            vo.setScopeName("暂未配置适用范围");
        }
        vo.setMemberCount(countCurrentGroupMembers(vo.getId()));
        return vo;
    }

    /**
     * 逻辑删除考勤组现有成员关系。
     *
     * @param groupId 考勤组ID
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus)
     */
    private void logicDeleteGroupMembers(Long groupId) {
        attendanceGroupMemberMapper.delete(new LambdaQueryWrapper<AttendanceGroupMemberEntity>()
                .eq(AttendanceGroupMemberEntity::getGroupId, groupId)
                .eq(AttendanceGroupMemberEntity::getIsDeleted, 0));
    }

    /**
     * 判断考勤组适用范围是否发生变化。
     *
     * @param previousScopeType 变更前范围类型
     * @param previousScopeValue 变更前范围值
     * @param currentScopeType 变更后范围类型
     * @param currentScopeValue 变更后范围值
     * @return 是否发生变化
     * 本方法使用的工具类: Objects(JDK),JSONUtil(hutool)
     */
    private boolean isGroupScopeChanged(String previousScopeType,
                                        String previousScopeValue,
                                        String currentScopeType,
                                        String currentScopeValue) {
        String normalizedPreviousScopeType = normalizeScopeType(previousScopeType);
        String normalizedCurrentScopeType = normalizeScopeType(currentScopeType);
        if (!Objects.equals(normalizedPreviousScopeType, normalizedCurrentScopeType)) {
            return true;
        }
        if (normalizedPreviousScopeType == null) {
            return false;
        }
        // 解析变更前范围值
        JSONObject previousScopeJson = parseScopeValue(previousScopeValue);
        // 解析当前范围值
        JSONObject currentScopeJson = parseScopeValue(currentScopeValue);
        return switch (normalizedPreviousScopeType) {
            case GROUP_SCOPE_DEPT -> !Objects.equals(
                    normalizeIds(getLongList(previousScopeJson, "deptIds")),
                    normalizeIds(getLongList(currentScopeJson, "deptIds"))
            );
            case GROUP_SCOPE_POST -> !Objects.equals(
                    previousScopeJson.getLong("postId"),
                    currentScopeJson.getLong("postId")
            );
            case GROUP_SCOPE_EMPLOYEE -> !Objects.equals(
                    previousScopeJson.getLong("deptId"),
                    currentScopeJson.getLong("deptId")
            ) || !Objects.equals(
                    normalizeIds(getLongList(previousScopeJson, "employeeIds")),
                    normalizeIds(getLongList(currentScopeJson, "employeeIds"))
            );
            default -> !Objects.equals(previousScopeJson.toString(), currentScopeJson.toString());
        };
    }

    /**
     * 插入考勤组成员关系。
     *
     * @param groupId     考勤组ID
     * @param employeeIds 员工ID列表
     * 本方法使用的工具类: LocalDate(JDK)
     */
    private void insertGroupMembers(Long groupId, List<Long> employeeIds) {
        LocalDate effectiveStartDate = LocalDate.now();
        for (Long employeeId : employeeIds) {
            AttendanceGroupMemberEntity member = new AttendanceGroupMemberEntity();
            member.setGroupId(groupId);
            member.setEmployeeId(employeeId);
            member.setEffectiveStartDate(effectiveStartDate);
            member.setStatus(1);
            member.setRemark("考勤组适用范围自动生成");
            attendanceGroupMemberMapper.insert(member);
        }
    }

    /**
     * 按部门查询有效员工。
     *
     * @param deptIds 部门ID列表
     * @return 员工快照列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus)
     */
    private List<EmployeeSnapshotEntity> listEmployeesByDeptIds(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return List.of();
        }
        return listActiveEmployees(new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                .in(EmployeeSnapshotEntity::getDeptId, deptIds));
    }

    /**
     * 按职位查询有效员工。
     *
     * @param postId 职位ID
     * @return 员工快照列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus)
     */
    private List<EmployeeSnapshotEntity> listEmployeesByPostId(Long postId) {
        if (postId == null) {
            return List.of();
        }
        return listActiveEmployees(new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                .eq(EmployeeSnapshotEntity::getPostId, postId));
    }

    /**
     * 按员工ID查询有效员工。
     *
     * @param employeeIds 员工ID列表
     * @return 员工快照列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus)
     */
    private List<EmployeeSnapshotEntity> listEmployeesByIds(List<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return List.of();
        }
        return listActiveEmployees(new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                .in(EmployeeSnapshotEntity::getId, employeeIds));
    }

    /**
     * 查询有效员工。
     *
     * @param wrapper 查询条件
     * @return 员工快照列表
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus),Collections(JDK)
     */
    private List<EmployeeSnapshotEntity> listActiveEmployees(LambdaQueryWrapper<EmployeeSnapshotEntity> wrapper) {
        wrapper.eq(EmployeeSnapshotEntity::getIsDeleted, 0)
                .ne(EmployeeSnapshotEntity::getEmploymentStatus, 4)
                .orderByAsc(EmployeeSnapshotEntity::getId);
        List<EmployeeSnapshotEntity> employees = employeeSnapshotMapper.selectList(wrapper);
        return employees == null ? Collections.emptyList() : employees;
    }

    /**
     * 统计考勤组当前有效成员数量。
     *
     * @param groupId 考勤组ID
     * @return 成员数量
     * 本方法使用的工具类: LambdaQueryWrapper(mybatis-plus)
     */
    private Integer countCurrentGroupMembers(Long groupId) {
        if (groupId == null) {
            return 0;
        }
        Long count = attendanceGroupMemberMapper.selectCount(new LambdaQueryWrapper<AttendanceGroupMemberEntity>()
                .eq(AttendanceGroupMemberEntity::getGroupId, groupId)
                .eq(AttendanceGroupMemberEntity::getStatus, 1)
                .eq(AttendanceGroupMemberEntity::getIsDeleted, 0));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 解析适用范围 JSON。
     *
     * @param scopeValue 适用范围 JSON 字符串
     * @return JSON 对象
     * 本方法使用的工具类: JSONUtil(hutool)
     */
    private JSONObject parseScopeValue(String scopeValue) {
        if (StrUtil.isBlank(scopeValue) || !JSONUtil.isTypeJSON(scopeValue)) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(scopeValue);
        } catch (Exception ex) {
            log.warn("parse attendance group scope value failed, scopeValue={}", scopeValue, ex);
            return new JSONObject();
        }
    }

    /**
     * 规范化适用范围类型。
     *
     * @param scopeType 范围类型
     * @return 规范化后的范围类型
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private String normalizeScopeType(String scopeType) {
        if (StrUtil.isBlank(scopeType)) {
            return null;
        }
        return scopeType.trim().toUpperCase();
    }

    /**
     * 从 JSON 中读取 Long 列表。
     *
     * @param json JSON 对象
     * @param key  字段名
     * @return Long 列表
     * 本方法使用的工具类: JSONArray(hutool)
     */
    private List<Long> getLongList(JSONObject json, String key) {
        JSONArray array = json.getJSONArray(key);
        if (array == null || array.isEmpty()) {
            return List.of();
        }
        return array.stream()
                .map(value -> value == null ? null : Long.valueOf(String.valueOf(value)))
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    /**
     * 规范化 ID 列表。
     *
     * @param ids ID 列表
     * @return 去重后的 ID 列表
     * 本方法使用的工具类: 无
     */
    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
    }

    /**
     * 构建部门范围展示名称。
     *
     * @param deptIds 部门ID列表
     * @return 展示名称
     * 本方法使用的工具类: DeptService(hrms-system-organization),Collectors(JDK)
     */
    private String buildDeptScopeName(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return "暂未配置适用范围";
        }
        return deptIds.stream()
                .map(this::resolveDeptNameWithoutCache)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining("、"));
    }

    /**
     * 构建指定员工范围展示名称。
     *
     * @param deptId      部门ID
     * @param employeeIds 员工ID列表
     * @return 展示名称
     * 本方法使用的工具类: DeptService(hrms-system-organization)
     */
    private String buildEmployeeScopeName(Long deptId, List<Long> employeeIds) {
        String deptName = resolveDeptNameWithoutCache(deptId);
        List<Long> normalizedEmployeeIds = normalizeIds(employeeIds);
        List<String> employeeNames = listEmployeesByIds(normalizedEmployeeIds).stream()
                .map(EmployeeSnapshotEntity::getEmployeeName)
                .filter(StrUtil::isNotBlank)
                .limit(3)
                .toList();
        String employeeSummary = employeeNames.isEmpty()
                ? "指定员工"
                : String.join("、", employeeNames) + (normalizedEmployeeIds.size() > employeeNames.size() ? "等" + normalizedEmployeeIds.size() + "人" : "");
        return StrUtil.isBlank(deptName) ? employeeSummary : deptName + " / " + employeeSummary;
    }

    /**
     * 解析部门名称。
     *
     * @param deptId 部门ID
     * @return 部门名称
     * 本方法使用的工具类: DeptService(hrms-system-organization)
     */
    private String resolveDeptNameWithoutCache(Long deptId) {
        if (deptId == null) {
            return null;
        }
        try {
            DeptDetailVO dept = deptService.getDeptById(deptId);
            return dept == null ? null : dept.getDeptName();
        } catch (Exception ex) {
            log.warn("resolve attendance group dept name failed, deptId={}", deptId, ex);
            return null;
        }
    }

    /**
     * 解析职位名称。
     *
     * @param postId 职位ID
     * @return 职位名称
     * 本方法使用的工具类: PostService(hrms-system-organization)
     */
    private String resolvePostName(Long postId) {
        if (postId == null) {
            return "暂未配置适用范围";
        }
        try {
            PostVO post = postService.getPostById(postId);
            return post == null ? "职位 " + postId : post.getPostName();
        } catch (Exception ex) {
            log.warn("resolve attendance group post name failed, postId={}", postId, ex);
            return "职位 " + postId;
        }
    }

    /**
     * 查询必定存在的考勤组。
     *
     * @param id 考勤组ID
     * @return 考勤组实体
     * 本方法使用的工具类: GlobalException(hrms-common)
     */
    private AttendanceGroupEntity getRequiredAttendanceGroup(Long id) {
        AttendanceGroupEntity entity = attendanceGroupMapper.selectById(id);
        if (entity == null) {
            throw new GlobalException(ATTENDANCE_GROUP_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 删除考勤组规则缓存。
     *
     * @param groupId 考勤组ID
     * 本方法使用的工具类: AttendanceCacheKeys(本模块cache包),StringRedisTemplate(spring-data-redis)
     */
    private void evictGroupRuleCache(Long groupId) {
        if (groupId == null) {
            return;
        }
        try {
            stringRedisTemplate.delete(AttendanceCacheKeys.groupRule(groupId));
        } catch (Exception ex) {
            log.warn("delete attendance group rule cache failed, groupId={}", groupId, ex);
        }
    }

    /**
     * 删除考勤组分页缓存。
     */
    private void evictAttendanceGroupPageCache() {
        try {
            Set<String> keys = stringRedisTemplate.keys(AttendanceCacheKeys.attendanceGroupPagePattern());
            if (keys == null || keys.isEmpty()) {
                return;
            }
            stringRedisTemplate.delete(keys);
        } catch (Exception ex) {
            log.warn("delete attendance group page cache failed");
        }
    }

    /**
     * 构建考勤组分页缓存查询键。
     *
     * @param queryDTO 查询参数
     * @return 查询键
     */
    private String buildAttendanceGroupPageCacheKey(AttendanceGroupQueryDTO queryDTO) {
        return String.join(":",
                String.valueOf(queryDTO.getPageNum()),
                String.valueOf(queryDTO.getPageSize()),
                normalizeCacheValue(queryDTO.getGroupName()),
                String.valueOf(queryDTO.getStatus() == null ? "null" : queryDTO.getStatus()));
    }

    /**
     * 构建考勤记录分页缓存查询键。
     *
     * @param groupId 考勤组 ID
     * @param queryDTO 查询参数
     * @param dateRange 日期范围
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 查询键
     */
    private String buildAttendanceGroupRecordPageCacheKey(Long groupId,
                                                          AttendanceGroupRecordQueryDTO queryDTO,
                                                          AttendanceDateRange dateRange,
                                                          int pageNum,
                                                          int pageSize) {
        return String.join(":",
                String.valueOf(groupId),
                normalizeCacheValue(queryDTO.getYearMonth()),
                String.valueOf(dateRange.startDate()),
                String.valueOf(dateRange.endDate()),
                String.valueOf(queryDTO.getDepartmentId() == null ? "null" : queryDTO.getDepartmentId()),
                normalizeCacheValue(queryDTO.getKeyword()),
                normalizeCacheValue(queryDTO.getStatus()),
                String.valueOf(pageNum),
                String.valueOf(pageSize));
    }

    /**
     * 构建请假管理分页缓存查询键。
     *
     * @param queryDTO 查询参数
     * @param yearMonth 查询月份
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 查询键
     */
    private String buildAttendanceLeaveManagePageCacheKey(AttendanceLeaveManageQueryDTO queryDTO,
                                                          YearMonth yearMonth,
                                                          int pageNum,
                                                          int pageSize) {
        return String.join(":",
                yearMonth.toString(),
                String.valueOf(queryDTO.getDeptId() == null ? "null" : queryDTO.getDeptId()),
                normalizeCacheValue(queryDTO.getKeyword()),
                String.valueOf(queryDTO.getApprovalStatus() == null ? "null" : queryDTO.getApprovalStatus()),
                String.valueOf(pageNum),
                String.valueOf(pageSize));
    }

    /**
     * 构建考勤统计看板缓存查询键。
     *
     * @param yearMonth 月份
     * @param deptId 部门 ID
     * @return 查询键
     */
    private String buildAttendanceSummaryDashboardCacheKey(String yearMonth, Long deptId) {
        return String.join(":",
                normalizeCacheValue(yearMonth),
                String.valueOf(deptId == null ? "null" : deptId));
    }

    /**
     * 标准化缓存字段值，避免空值和空白值生成不同 key。
     *
     * @param value 原始值
     * @return 标准化后的值
     */
    private String normalizeCacheValue(String value) {
        return StrUtil.isBlank(value) ? "blank" : value.trim();
    }

    /**
     * 读取普通对象缓存。
     *
     * @param cacheKey 缓存键
     * @param beanClass 目标类型
     * @param warnMessage 告警日志
     * @param <T> 返回类型
     * @return 缓存结果
     */
    private <T> T loadCacheValue(String cacheKey, Class<T> beanClass, String warnMessage) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isBlank(cached)) {
                return null;
            }
            return JSONUtil.toBean(cached, beanClass);
        } catch (Exception ex) {
            log.warn(warnMessage, cacheKey, ex);
            return null;
        }
    }

    /**
     * 读取泛型对象缓存。
     *
     * @param cacheKey 缓存键
     * @param typeReference 目标泛型
     * @param warnMessage 告警日志
     * @param <T> 返回类型
     * @return 缓存结果
     */
    private <T> T loadCacheValue(String cacheKey, TypeReference<T> typeReference, String warnMessage) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isBlank(cached)) {
                return null;
            }
            return JSONUtil.toBean(cached, typeReference, false);
        } catch (Exception ex) {
            log.warn(warnMessage, cacheKey, ex);
            return null;
        }
    }

    /**
     * 写入查询缓存。
     *
     * @param cacheKey 缓存键
     * @param value 缓存值
     * @param warnMessage 告警日志
     */
    private void saveCacheValue(String cacheKey, Object value, String warnMessage) {
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(value), Duration.ofDays(2));
        } catch (Exception ex) {
            log.warn(warnMessage, cacheKey, ex);
        }
    }

    /**
     * 删除指定缓存键。
     *
     * @param cacheKey 缓存键
     * @param warnMessage 告警日志
     */
    private void deleteCacheKey(String cacheKey, String warnMessage) {
        try {
            stringRedisTemplate.delete(cacheKey);
        } catch (Exception ex) {
            log.warn(warnMessage, cacheKey, ex);
        }
    }

    /**
     * 删除请假审批相关的查询缓存。
     */
    private void evictAttendanceQueryCachesForLeaveApproval() {
        deleteCacheByPattern(AttendanceCacheKeys.leaveManagePagePattern(), "delete attendance leave manage page cache failed");
        deleteCacheByPattern(AttendanceCacheKeys.summaryDashboardPattern(), "delete attendance summary dashboard cache failed");
        deleteCacheByPattern(AttendanceCacheKeys.attendanceGroupRecordPagePattern(), "delete attendance group record page cache failed");
    }

    /**
     * 按模式批量删除缓存。
     *
     * @param pattern 缓存模式
     * @param warnMessage 告警日志
     */
    private void deleteCacheByPattern(String pattern, String warnMessage) {
        try {
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                return;
            }
            stringRedisTemplate.delete(keys);
        } catch (Exception ex) {
            log.warn(warnMessage, ex);
        }
    }

    /**
     * 考勤统计部门范围。
     *
     * @param targetDeptIds 目标部门ID集合，null表示全量
     */
    private record AttendanceDashboardScope(Set<Long> targetDeptIds) {
    }

    /**
     * 请假管理部门范围。
     *
     * @param targetDeptIds 目标部门ID集合，null表示全量
     */
    private record LeaveManageScope(Set<Long> targetDeptIds) {
    }

    /**
     * 审批摘要。
     *
     * @param currentNodeName     当前节点名称
     * @param currentApproverName 当前审批人名称
     */
    private record ApprovalBrief(String currentNodeName, String currentApproverName) {

        /**
         * 创建空审批摘要。
         *
         * @return 空审批摘要
         * 本方法使用的工具类: 无
         */
        private static ApprovalBrief empty() {
            return new ApprovalBrief(null, null);
        }
    }

    /**
     * 考勤统计累加器。
     */
    private static class AttendanceDashboardAccumulator {

        /**
         * 每日统计。
         */
        private final Map<LocalDate, DashboardMutableStats> dailyStats;

        /**
         * 部门统计。
         */
        private final Map<Long, DashboardMutableStats> deptStats = new LinkedHashMap<>();

        /**
         * 员工异常统计。
         */
        private final Map<Long, DashboardEmployeeStats> employeeStats = new LinkedHashMap<>();

        /**
         * 创建考勤统计累加器。
         *
         * @param workdays 工作日列表
         * 本方法使用的工具类: LinkedHashMap(JDK)
         */
        private AttendanceDashboardAccumulator(List<LocalDate> workdays) {
            this.dailyStats = new LinkedHashMap<>();
            for (LocalDate workday : workdays) {
                this.dailyStats.put(workday, new DashboardMutableStats());
            }
        }
    }

    /**
     * 可变考勤统计值。
     */
    private static class DashboardMutableStats {

        /**
         * 应出勤人天。
         */
        private int expectedDays;

        /**
         * 实际出勤人天。
         */
        private int actualDays;

        /**
         * 迟到次数。
         */
        private int lateCount;

        /**
         * 早退次数。
         */
        private int earlyLeaveCount;

        /**
         * 缺勤次数。
         */
        private int absentCount;
    }

    /**
     * 员工异常统计值。
     */
    private static class DashboardEmployeeStats {

        /**
         * 员工快照。
         */
        private final EmployeeSnapshotEntity employee;

        /**
         * 迟到次数。
         */
        private int lateCount;

        /**
         * 早退次数。
         */
        private int earlyLeaveCount;

        /**
         * 缺勤次数。
         */
        private int absentCount;

        /**
         * 创建员工异常统计值。
         *
         * @param employee 员工快照
         * 本方法使用的工具类: 无
         */
        private DashboardEmployeeStats(EmployeeSnapshotEntity employee) {
            this.employee = employee;
        }

        /**
         * 计算异常总次数。
         *
         * @return 异常总次数
         * 本方法使用的工具类: 无
         */
        private int abnormalCount() {
            return lateCount + earlyLeaveCount + absentCount;
        }
    }
}
