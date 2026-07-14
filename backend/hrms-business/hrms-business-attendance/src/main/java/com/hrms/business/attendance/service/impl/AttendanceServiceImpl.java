package com.hrms.business.attendance.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.attendance.cache.AttendanceCacheKeys;
import com.hrms.business.attendance.convert.AttendanceGroupConvert;
import com.hrms.business.attendance.dto.AttendanceClockRequestDTO;
import com.hrms.business.attendance.dto.AttendanceCorrectionCreateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.dto.LeaveCreateRequestDTO;
import com.hrms.business.attendance.entity.AttendanceCorrectionEntity;
import com.hrms.business.attendance.entity.AttendanceGroupEntity;
import com.hrms.business.attendance.entity.AttendanceRecordEntity;
import com.hrms.business.attendance.entity.EmployeeSnapshotEntity;
import com.hrms.business.attendance.entity.LeaveRequestEntity;
import com.hrms.business.attendance.entity.DictDataEntity;
import com.hrms.business.attendance.enums.ClockPeriodEnum;
import com.hrms.business.attendance.mapper.AttendanceGroupMapper;
import com.hrms.business.attendance.mapper.AttendanceCorrectionMapper;
import com.hrms.business.attendance.mapper.AttendanceRecordMapper;
import com.hrms.business.attendance.mapper.EmployeeSnapshotMapper;
import com.hrms.business.attendance.mapper.LeaveRequestMapper;
import com.hrms.business.attendance.mapper.DictDataMapper;
import com.hrms.business.attendance.mq.AttendanceClockCreatedEvent;
import com.hrms.business.attendance.mq.AttendanceClockEventHandler;
import com.hrms.business.attendance.mq.AttendanceMqConstants;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceClockVO;
import com.hrms.business.attendance.vo.AttendanceCalendarDayVO;
import com.hrms.business.attendance.vo.AttendanceCalendarVO;
import com.hrms.business.attendance.vo.AttendanceCorrectionCreateVO;
import com.hrms.business.attendance.vo.LeaveTypeVO;
import com.hrms.business.attendance.vo.LeaveBalanceVO;
import com.hrms.business.attendance.vo.LeaveCreateVO;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.math.RoundingMode;

/**
 * 考勤管理服务实现。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private static final ErrorCode ATTENDANCE_GROUP_NOT_FOUND = new ErrorCode(40052, "考勤组不存在");

    private static final ErrorCode ATTENDANCE_EMPLOYEE_NOT_FOUND = new ErrorCode(40053, "当前用户未关联员工档案");

    private static final ErrorCode ATTENDANCE_CLOCK_DUPLICATE = new ErrorCode(40054, "当前时段已打卡");

    private static final ErrorCode ATTENDANCE_CLOCK_RANGE_INVALID = new ErrorCode(40055, "不在允许的打卡范围内");

    private static final ErrorCode ATTENDANCE_CORRECTION_DUPLICATE = new ErrorCode(40056, "当前日期和类型已有审批中的补卡申请");

    private static final ErrorCode LEAVE_DAYS_INVALID = new ErrorCode(40057, "请假天数必须大于0且不超过30天");

    private final AttendanceGroupMapper attendanceGroupMapper;

    private final AttendanceRecordMapper attendanceRecordMapper;

    private final AttendanceCorrectionMapper attendanceCorrectionMapper;

    private final EmployeeSnapshotMapper employeeSnapshotMapper;

    private final LeaveRequestMapper leaveRequestMapper;

    private final DictDataMapper dictDataMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RabbitTemplate rabbitTemplate;

    private final AttendanceClockEventHandler attendanceClockEventHandler;

    @Override
    public PageResult<AttendanceGroupPageVO> pageAttendanceGroups(AttendanceGroupQueryDTO queryDTO) {
        Page<AttendanceGroupEntity> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<AttendanceGroupEntity> wrapper = new LambdaQueryWrapper<AttendanceGroupEntity>()
                .like(StrUtil.isNotBlank(queryDTO.getGroupName()), AttendanceGroupEntity::getGroupName, queryDTO.getGroupName())
                .eq(queryDTO.getStatus() != null, AttendanceGroupEntity::getStatus, queryDTO.getStatus())
                .orderByDesc(AttendanceGroupEntity::getCreateTime)
                .orderByDesc(AttendanceGroupEntity::getId);
        Page<AttendanceGroupEntity> resultPage = attendanceGroupMapper.selectPage(page, wrapper);
        List<AttendanceGroupPageVO> records = resultPage.getRecords().stream()
                .map(AttendanceGroupConvert::toPageVO)
                .toList();
        return PageResult.of(records, resultPage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public AttendanceGroupPageVO createAttendanceGroup(AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        AttendanceGroupEntity entity = AttendanceGroupConvert.toEntity(requestDTO);
        attendanceGroupMapper.insert(entity);
        evictGroupRuleCache(entity.getId());
        return AttendanceGroupConvert.toPageVO(entity);
    }

    @Override
    public AttendanceGroupPageVO updateAttendanceGroup(Long id, AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        AttendanceGroupEntity entity = getRequiredAttendanceGroup(id);
        AttendanceGroupConvert.fillEntity(entity, requestDTO);
        attendanceGroupMapper.updateById(entity);
        evictGroupRuleCache(id);
        return AttendanceGroupConvert.toPageVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceClockVO clock(AttendanceClockRequestDTO requestDTO, String clientIp) {
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        AttendanceGroupEntity group = tempResolveEmployeeAttendanceGroup(employee);
        validateClockRange(group, requestDTO, clientIp);

        LocalDateTime now = LocalDateTime.now();
        LocalDate recordDate = now.toLocalDate();
        AttendanceRecordEntity existing = attendanceRecordMapper.selectByEmployeeAndDate(employee.getId(), recordDate);
        ClockPeriodEnum period = resolveClockPeriod(requestDTO, existing);
        String status = calculateClockStatus(group, period, now.toLocalTime());
        AttendanceRecordEntity record = buildClockRecord(employee.getId(), group.getId(), recordDate, now, period, status, requestDTO, clientIp);

        persistClockRecord(existing, record, period);
        AttendanceClockCreatedEvent event = buildClockCreatedEvent(record, period, status, now, requestDTO.getDeviceInfo());
        publishClockCreatedEvent(event);
        return buildClockVO(record, period, status, now);
    }

    @Override
    public AttendanceCalendarVO getMyCalendar(String yearMonth) {
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        YearMonth parsedMonth = YearMonth.parse(yearMonth);
        String cacheKey = AttendanceCacheKeys.monthCalendar(employee.getId(), parsedMonth.toString());
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cached)) {
            return JSONUtil.toBean(cached, AttendanceCalendarVO.class);
        }
        AttendanceCalendarVO calendar = buildCalendarFromDatabase(employee.getId(), parsedMonth);
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(calendar), Duration.ofHours(6));
        return calendar;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceCorrectionCreateVO createCorrection(AttendanceCorrectionCreateRequestDTO requestDTO) {
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        ClockPeriodEnum period = ClockPeriodEnum.parse(requestDTO.getClockType());
        if (period == null) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "补卡类型不正确");
        }
        checkCorrectionDuplicate(employee.getId(), requestDTO.getDate(), period);
        AttendanceRecordEntity record = getOrCreateCorrectionRecord(employee.getId(), requestDTO.getDate());
        // approvalService.startAttendanceCorrectionApproval(correction); 本接口需要调用 hrms-business-approval 模块的补卡审批发起方法。
        Long approvalInstanceId = tempStartCorrectionApproval(employee.getId(), record.getId(), requestDTO);

        AttendanceCorrectionEntity correction = new AttendanceCorrectionEntity();
        correction.setEmployeeId(employee.getId());
        correction.setRecordId(record.getId());
        correction.setCorrectionDate(requestDTO.getDate());
        correction.setCorrectionType(period.name());
        correction.setCorrectionReason(requestDTO.getReason());
        correction.setApprovalInstanceId(approvalInstanceId);
        correction.setApprovalStatus(1);
        attendanceCorrectionMapper.insert(correction);
        attendanceRecordMapper.updateCorrectionStatus(record.getId(), "PENDING");
        evictCalendarCache(employee.getId(), requestDTO.getDate());
        return buildCorrectionCreateVO(correction);
    }

    @Override
    public List<LeaveTypeVO> listLeaveTypes() {
        return dictDataMapper.selectList(new LambdaQueryWrapper<DictDataEntity>()
                        .eq(DictDataEntity::getDictType, "leave_type")
                        .eq(DictDataEntity::getStatus, 1)
                        .eq(DictDataEntity::getIsDeleted, 0)
                        .orderByAsc(DictDataEntity::getSort)
                        .orderByAsc(DictDataEntity::getId))
                .stream()
                .map(this::toLeaveTypeVO)
                .toList();
    }

    @Override
    public List<LeaveBalanceVO> listLeaveBalances() {
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        LocalDate yearStart = LocalDate.now().withDayOfYear(1);
        LocalDate yearEnd = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
        Map<String, BigDecimal> usedDays = leaveRequestMapper.selectList(new LambdaQueryWrapper<LeaveRequestEntity>()
                        .eq(LeaveRequestEntity::getEmployeeId, employee.getId())
                        .in(LeaveRequestEntity::getApprovalStatus, List.of(1, 2))
                        .ge(LeaveRequestEntity::getStartTime, yearStart.atStartOfDay())
                        .le(LeaveRequestEntity::getStartTime, yearEnd.atTime(LocalTime.MAX)))
                .stream()
                .collect(Collectors.groupingBy(LeaveRequestEntity::getLeaveType,
                        Collectors.reducing(BigDecimal.ZERO, LeaveRequestEntity::getTotalDays, BigDecimal::add)));
        return List.of(
                buildLeaveBalance("annual", "年假", calculateAnnualTotal(employee), usedDays.getOrDefault("annual", BigDecimal.ZERO)),
                buildLeaveBalance("sick", "病假", BigDecimal.valueOf(10), usedDays.getOrDefault("sick", BigDecimal.ZERO)),
                buildLeaveBalance("personal", "事假", BigDecimal.valueOf(5), usedDays.getOrDefault("personal", BigDecimal.ZERO))
        );
    }

    /**
     * 临时计算年假总额度。
     *
     * @param employee 员工快照
     * @return 年假总额度
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private BigDecimal calculateAnnualTotal(EmployeeSnapshotEntity employee) {
        // leaveAccountService.getBalance(employee.getId()); 本接口后续应替换为正式假期余额表或员工假期账户接口。
        if (employee.getHireDate() == null || employee.getHireDate().isAfter(LocalDate.now())) {
            return BigDecimal.ZERO;
        }
        int months = Math.max(1, LocalDate.now().getMonthValue() - employee.getHireDate().getMonthValue() + 1);
        return BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(12), 1, RoundingMode.DOWN);
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
        LeaveBalanceVO vo = new LeaveBalanceVO();
        vo.setLeaveType(type);
        vo.setLeaveTypeName(name);
        vo.setTotalDays(totalDays);
        vo.setUsedDays(usedDays);
        vo.setRemainingDays(totalDays.subtract(usedDays).max(BigDecimal.ZERO));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveCreateVO createLeave(LeaveCreateRequestDTO requestDTO) {
        EmployeeSnapshotEntity employee = getCurrentEmployeeSnapshot();
        String leaveType = resolveLeaveType(requestDTO);
        BigDecimal totalDays = calculateLeaveDays(requestDTO);
        if (totalDays.compareTo(BigDecimal.ZERO) <= 0 || totalDays.compareTo(BigDecimal.valueOf(30)) > 0) {
            throw new GlobalException(LEAVE_DAYS_INVALID);
        }
        // approvalService.startLeaveApproval(leaveRequest); 本接口需要调用 hrms-business-approval 模块的请假审批发起方法。
        Long approvalInstanceId = tempStartLeaveApproval(employee.getId(), requestDTO);
        LeaveRequestEntity entity = new LeaveRequestEntity();
        entity.setEmployeeId(employee.getId());
        entity.setLeaveType(leaveType);
        entity.setStartTime(toPeriodTime(requestDTO.getStartDate(), requestDTO.getStartPeriod(), true));
        entity.setEndTime(toPeriodTime(requestDTO.getEndDate(), requestDTO.getEndPeriod(), false));
        entity.setTotalDays(totalDays);
        entity.setTotalHours(totalDays.multiply(BigDecimal.valueOf(8)));
        entity.setLeaveReason(requestDTO.getReason());
        entity.setAttachmentUrl(resolveAttachment(requestDTO));
        entity.setApprovalInstanceId(approvalInstanceId);
        entity.setApprovalStatus(1);
        leaveRequestMapper.insert(entity);
        evictCalendarCache(employee.getId(), requestDTO.getStartDate());
        return buildLeaveCreateVO(entity);
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
    private Long tempStartLeaveApproval(Long employeeId, LeaveCreateRequestDTO requestDTO) {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 构建请假创建结果。
     *
     * @param entity 请假申请
     * @return 创建结果
     * 本方法使用的工具类: 无
     */
    private LeaveCreateVO buildLeaveCreateVO(LeaveRequestEntity entity) {
        LeaveCreateVO vo = new LeaveCreateVO();
        vo.setId(entity.getId());
        vo.setApprovalInstanceId(entity.getApprovalInstanceId());
        vo.setApprovalStatus(entity.getApprovalStatus());
        return vo;
    }

    /**
     * 转换请假类型视图。
     *
     * @param entity 字典数据
     * @return 请假类型视图
     * 本方法使用的工具类: 无
     */
    private LeaveTypeVO toLeaveTypeVO(DictDataEntity entity) {
        LeaveTypeVO vo = new LeaveTypeVO();
        vo.setId(entity.getId());
        vo.setLabel(entity.getDictLabel());
        vo.setValue(entity.getDictValue());
        return vo;
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
        AttendanceGroupEntity group = tempResolveEmployeeAttendanceGroup(new EmployeeSnapshotEntity());
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
    private Long tempStartCorrectionApproval(Long employeeId, Long recordId, AttendanceCorrectionCreateRequestDTO requestDTO) {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 构建补卡创建响应。
     *
     * @param correction 补卡申请
     * @return 创建响应
     * 本方法使用的工具类: 无
     */
    private AttendanceCorrectionCreateVO buildCorrectionCreateVO(AttendanceCorrectionEntity correction) {
        AttendanceCorrectionCreateVO vo = new AttendanceCorrectionCreateVO();
        vo.setId(correction.getId());
        vo.setRecordId(correction.getRecordId());
        vo.setApprovalInstanceId(correction.getApprovalInstanceId());
        vo.setApprovalStatus(correction.getApprovalStatus());
        return vo;
    }

    /**
     * 删除个人月历缓存。
     *
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
        AttendanceCalendarVO calendar = new AttendanceCalendarVO();
        calendar.setEmployeeId(employeeId);
        calendar.setYearMonth(parsedMonth.toString());
        calendar.setDays(days);
        return calendar;
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
        AttendanceCalendarDayVO day = new AttendanceCalendarDayVO();
        day.setDate(date);
        day.setLeave(leaveDates.contains(date));
        if (record != null) {
            day.setClockInTime(record.getClockInTime());
            day.setClockOutTime(record.getClockOutTime());
            day.setClockInStatus(record.getClockInStatus());
            day.setClockOutStatus(record.getClockOutStatus());
        }
        day.setDayStatus(resolveDayStatus(day));
        return day;
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
     * 临时解析员工所属考勤组。
     *
     * @param employee 员工快照
     * @return 考勤组
     * 本方法使用的工具类: GlobalException(hrms-common)
     */
    private AttendanceGroupEntity tempResolveEmployeeAttendanceGroup(EmployeeSnapshotEntity employee) {
        // attendanceGroupMemberService.getEmployeeGroup(employee.getId()); 本接口需要调用考勤组成员/员工归属解析接口。
        AttendanceGroupEntity group = attendanceGroupMapper.selectOne(new LambdaQueryWrapper<AttendanceGroupEntity>()
                .eq(AttendanceGroupEntity::getStatus, 1)
                .orderByAsc(AttendanceGroupEntity::getId)
                .last("LIMIT 1"));
        if (group == null) {
            throw new GlobalException(ATTENDANCE_GROUP_NOT_FOUND);
        }
        return group;
    }

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
            JSONObject json = JSONUtil.parseObj(gpsScope);
            double centerLat = json.getDouble("latitude", json.getDouble("lat", 0D));
            double centerLng = json.getDouble("longitude", json.getDouble("lng", 0D));
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
        if (ClockPeriodEnum.CLOCK_IN.equals(period)) {
            LocalTime lateLine = group.getWorkStartTime().plusMinutes(group.getLateThresholdMinutes());
            return clockTime.isAfter(lateLine) ? "LATE" : "NORMAL";
        }
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
            int updated = ClockPeriodEnum.CLOCK_IN.equals(period)
                    ? attendanceRecordMapper.updateClockIn(record)
                    : attendanceRecordMapper.updateClockOut(record);
            if (updated == 0) {
                throw new GlobalException(ATTENDANCE_CLOCK_DUPLICATE);
            }
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
        AttendanceClockCreatedEvent event = new AttendanceClockCreatedEvent();
        event.setMessageId(IdUtil.fastSimpleUUID());
        event.setEmployeeId(record.getEmployeeId());
        event.setGroupId(record.getGroupId());
        event.setRecordId(record.getId());
        event.setRecordDate(record.getRecordDate());
        event.setPeriod(period.name());
        event.setStatus(status);
        event.setClockTime(clockTime);
        event.setDeviceInfo(deviceInfo);
        return event;
    }

    /**
     * 发布打卡成功事件。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: RabbitTemplate(spring-amqp)
     */
    private void publishClockCreatedEvent(AttendanceClockCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(AttendanceMqConstants.EXCHANGE, AttendanceMqConstants.CLOCK_CREATED_ROUTING_KEY, event);
        } catch (Exception ex) {
            log.warn("publish attendance.clock.created failed, use temp handler, event={}", event, ex);
            // attendanceClockCreatedProducer.send(event); 本接口需要调用考勤 MQ Producer 发送 attendance.clock.created。
            tempPublishClockCreatedEvent(event);
        }
    }

    /**
     * 临时处理打卡成功事件。
     *
     * @param event 打卡成功事件
     * 本方法使用的工具类: AttendanceClockEventHandler(本模块mq包)
     */
    private void tempPublishClockCreatedEvent(AttendanceClockCreatedEvent event) {
        try {
            attendanceClockEventHandler.handleClockCreatedEvent(event);
        } catch (Exception ex) {
            log.warn("temp handle attendance.clock.created failed, event={}", event, ex);
        }
    }

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
        AttendanceClockVO vo = new AttendanceClockVO();
        vo.setRecordId(record.getId());
        vo.setEmployeeId(record.getEmployeeId());
        vo.setGroupId(record.getGroupId());
        vo.setRecordDate(record.getRecordDate());
        vo.setPeriod(period.name());
        vo.setStatus(status);
        vo.setClockTime(clockTime);
        return vo;
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

    @Override
    public Object getMonthlySummary(Long employeeId, Integer year, Integer month) {
        return null;
    }

    @Override
    public void checkIn(Long employeeId, Integer type) {
    }

    @Override
    public void applyLeave(Long employeeId, String leaveType, String startDate, String endDate, String reason) {
    }
}
