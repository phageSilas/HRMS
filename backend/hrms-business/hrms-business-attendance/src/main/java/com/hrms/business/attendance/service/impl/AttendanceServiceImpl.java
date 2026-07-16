package com.hrms.business.attendance.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.attendance.cache.AttendanceCacheKeys;
import com.hrms.business.attendance.convert.AttendanceGroupConvert;
import com.hrms.business.attendance.dto.AttendanceClockRequestDTO;
import com.hrms.business.attendance.dto.AttendanceCorrectionCreateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
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
import com.hrms.business.attendance.enums.ClockPeriodEnum;
import com.hrms.business.attendance.mapper.AttendanceGroupMapper;
import com.hrms.business.attendance.mapper.AttendanceCorrectionMapper;
import com.hrms.business.attendance.mapper.AttendanceGroupMemberMapper;
import com.hrms.business.attendance.mapper.AttendanceRecordMapper;
import com.hrms.business.attendance.mapper.AttendanceEmployeeSnapshotMapper;
import com.hrms.business.attendance.mapper.LeaveBalanceMapper;
import com.hrms.business.attendance.mapper.LeaveRequestMapper;
import com.hrms.business.attendance.mapper.AttendanceDictDataMapper;
import com.hrms.business.attendance.mq.AttendanceClockCreatedProducer;
import com.hrms.business.attendance.mq.AttendanceClockCreatedEvent;
import com.hrms.business.attendance.mq.AttendanceClockEventHandler;
import com.hrms.business.attendance.mq.AttendanceMonthlyStatGenerateMessage;
import com.hrms.business.attendance.mq.AttendanceMonthlyStatGenerateProducer;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceClockVO;
import com.hrms.business.attendance.vo.AttendanceCalendarDayVO;
import com.hrms.business.attendance.vo.AttendanceCalendarVO;
import com.hrms.business.attendance.vo.AttendanceCorrectionCreateVO;
import com.hrms.business.attendance.vo.LeaveTypeVO;
import com.hrms.business.attendance.vo.LeaveBalanceVO;
import com.hrms.business.attendance.vo.LeaveCreateVO;
import com.hrms.business.attendance.vo.MonthlyStatGenerateVO;
import com.hrms.business.attendance.vo.AttendancePayrollSourceVO;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Collections;

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

    private final AttendanceGroupMemberMapper attendanceGroupMemberMapper;

    private final AttendanceRecordMapper attendanceRecordMapper;

    private final AttendanceCorrectionMapper attendanceCorrectionMapper;

    private final AttendanceEmployeeSnapshotMapper employeeSnapshotMapper;

    private final LeaveBalanceMapper leaveBalanceMapper;

    private final LeaveRequestMapper leaveRequestMapper;

    private final AttendanceDictDataMapper dictDataMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final AttendanceClockCreatedProducer attendanceClockCreatedProducer;

    private final AttendanceMonthlyStatGenerateProducer attendanceMonthlyStatGenerateProducer;

    private final AttendanceClockEventHandler attendanceClockEventHandler;

    private final ApprovalEngine approvalEngine;

    /**
     * 分页查询考勤组。
     * @param queryDTO 查询参数
     * @return
     */
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

    /**
     * 创建考勤组。
     * @param requestDTO 创建参数
     * @return
     */
    @Override
    public AttendanceGroupPageVO createAttendanceGroup(AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        AttendanceGroupEntity entity = AttendanceGroupConvert.toEntity(requestDTO);
        attendanceGroupMapper.insert(entity);
        evictGroupRuleCache(entity.getId());
        return AttendanceGroupConvert.toPageVO(entity);

    }

    /**
     * 更新考勤组。
     * @param id 考勤组ID
     * @param requestDTO 更新参数
      * @return 更新后的考勤组
     */

    @Override
    public AttendanceGroupPageVO updateAttendanceGroup(Long id, AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        AttendanceGroupEntity entity = getRequiredAttendanceGroup(id);
        AttendanceGroupConvert.fillEntity(entity, requestDTO);
        attendanceGroupMapper.updateById(entity);
        evictGroupRuleCache(id);
        return AttendanceGroupConvert.toPageVO(entity);
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
        validateClockRange(group, requestDTO, clientIp);

        AttendanceRecordEntity existing = attendanceRecordMapper.selectByEmployeeAndDate(employee.getId(), recordDate);
        ClockPeriodEnum period = resolveClockPeriod(requestDTO, existing);
        String status = calculateClockStatus(group, period, now.toLocalTime());
        AttendanceRecordEntity record = buildClockRecord(employee.getId(), group.getId(), recordDate, now, period, status, requestDTO, clientIp);

        persistClockRecord(existing, record, period);
        AttendanceClockCreatedEvent event = buildClockCreatedEvent(record, period, status, now, requestDTO.getDeviceInfo());
        publishClockCreatedEvent(event);
        return buildClockVO(record, period, status, now);
    }

    /**
     * 获取个人日历。
     * @param yearMonth 年月，格式为 yyyy-MM
     * @return 日历数据
     */
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
    /**
     * 创建补卡申请。
     * @param requestDTO 创建参数
     * @return 补卡申请结果
     */

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

        AttendanceCorrectionEntity correction = new AttendanceCorrectionEntity();
        correction.setEmployeeId(employee.getId());
        correction.setRecordId(record.getId());
        correction.setCorrectionDate(requestDTO.getDate());
        correction.setCorrectionType(period.name());
        correction.setCorrectionReason(requestDTO.getReason());
        correction.setApprovalStatus(1);
        attendanceCorrectionMapper.insert(correction);

        // TODO 跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起补卡审批。
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
                        .eq(DictDataEntity::getDictType, "leave_type")
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

        // TODO 跨模块调用已完成：当前调用 ApprovalEngine#startApproval(...) 发起请假审批。
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
        evictCalendarCache(employee.getId(), requestDTO.getStartDate());
        return buildLeaveCreateVO(entity);
    }

    /**
     * 生成月度统计。
     * @param requestDTO 生成参数
     * @return 月度统计结果
     */
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
     * 本方法使用的工具类: JSONUtil(hutool)
     */
    @Override
    public List<AttendancePayrollSourceVO> getPayrollSource(String month, List<Long> employeeIds) {
        List<EmployeeSnapshotEntity> employees = findStatEmployees(employeeIds);
        return employees.stream()
                .map(employee -> getPayrollSourceFromCache(month, employee))
                .toList();
    }

    /**
     * 从缓存读取薪资考勤数据，未命中时即时计算。
     *
     * @param month    月份
     * @param employee 员工快照
     * @return 薪资考勤数据
     * 本方法使用的工具类: JSONUtil(hutool),AttendanceCacheKeys(本模块cache包)
     */
    private AttendancePayrollSourceVO getPayrollSourceFromCache(String month, EmployeeSnapshotEntity employee) {
        String cached = stringRedisTemplate.opsForValue().get(AttendanceCacheKeys.monthStat(employee.getId(), month));
        if (StrUtil.isNotBlank(cached)) {
            return JSONUtil.toBean(cached, AttendancePayrollSourceVO.class);
        }
        return computePayrollSource(month, employee);
    }

    /**
     * 批量计算薪资考勤数据。
     *
     * @param month       月份
     * @param employeeIds 员工ID列表
     * @return 薪资考勤数据
     * 本方法使用的工具类: List(JDK)
     */
    private List<AttendancePayrollSourceVO> computePayrollSources(String month, List<Long> employeeIds) {
        return findStatEmployees(employeeIds).stream()
                .map(employee -> computePayrollSource(month, employee))
                .toList();
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
     * 计算工作日数量。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 工作日数量
     * 本方法使用的工具类: IntStream(JDK)
     */
    private int countWeekdays(LocalDate startDate, LocalDate endDate) {
        return (int) startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek().getValue() <= 5)
                .count();
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
                .clockInStatus(record == null ? null : record.getClockInStatus())
                .clockOutStatus(record == null ? null : record.getClockOutStatus())
                .build();
        return AttendanceCalendarDayVO.builder()
                .date(day.getDate())
                .leave(day.getLeave())
                .clockInTime(day.getClockInTime())
                .clockOutTime(day.getClockOutTime())
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
        return AttendanceClockVO.builder()
                .recordId(record.getId())
                .employeeId(record.getEmployeeId())
                .groupId(record.getGroupId())
                .recordDate(record.getRecordDate())
                .period(period.name())
                .status(status)
                .clockTime(clockTime)
                .build();
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
}
