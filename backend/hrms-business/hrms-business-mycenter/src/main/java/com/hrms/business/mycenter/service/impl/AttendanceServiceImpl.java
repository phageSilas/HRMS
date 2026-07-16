package com.hrms.business.mycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.business.mycenter.dto.AttendanceCalendarVO;
import com.hrms.business.mycenter.dto.AttendanceStatisticsVO;
import com.hrms.business.mycenter.dto.MakeupRecordVO;
import com.hrms.business.mycenter.dto.MakeupRequest;
import com.hrms.business.mycenter.dto.OvertimeRecordVO;
import com.hrms.business.mycenter.dto.OvertimeRequest;
import com.hrms.business.mycenter.entity.AttendanceCorrectionEntity;
import com.hrms.business.mycenter.entity.MyAttendanceRecordEntity;
import com.hrms.business.mycenter.mapper.MyCenterAttendanceCorrectionMapper;
import com.hrms.business.mycenter.mapper.MyAttendanceRecordMapper;
import com.hrms.business.mycenter.service.AttendanceService;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 个人考勤服务实现
 */
@Slf4j
@Service("myCenterAttendanceServiceImpl")
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final MyAttendanceRecordMapper attendanceRecordMapper;
    private final MyCenterAttendanceCorrectionMapper correctionMapper;

    /**
     * 加班记录内存存储（TODO: 后续对接数据库表 hr_attendance_overtime）
     */
    private final Map<Long, List<SimpleOvertimeRecord>> overtimeStore = new ConcurrentHashMap<>();
    private final AtomicLong overtimeIdSeq = new AtomicLong(1);

    /**
     * 简单的加班记录内部类（替代数据库实体）
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    private static class SimpleOvertimeRecord {
        private Long id;
        private Long employeeId;
        private LocalDateTime overtimeDate;
        private java.math.BigDecimal duration;
        private String reason;
        private Integer approvalStatus;
        private LocalDateTime createTime;
    }

    @Override
    public AttendanceCalendarVO getCalendar(Long employeeId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate firstDay = ym.atDay(1);
        LocalDate lastDay = ym.atEndOfMonth();

        // 查询该月所有考勤记录
        List<MyAttendanceRecordEntity> records = attendanceRecordMapper.selectList(
                new LambdaQueryWrapper<MyAttendanceRecordEntity>()
                        .eq(MyAttendanceRecordEntity::getEmployeeId, employeeId)
                        .between(MyAttendanceRecordEntity::getRecordDate, firstDay, lastDay)
        );

        // 按日期索引
        Map<LocalDate, MyAttendanceRecordEntity> recordMap = records.stream()
                .collect(Collectors.toMap(MyAttendanceRecordEntity::getRecordDate, r -> r, (a, b) -> b));

        // 构建每日日历
        List<AttendanceCalendarVO.AttendanceDayVO> days = new ArrayList<>();
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            AttendanceCalendarVO.AttendanceDayVO day = new AttendanceCalendarVO.AttendanceDayVO();
            day.setDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE));

            // 判断是否周末
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY;

            MyAttendanceRecordEntity record = recordMap.get(date);

            if (isWeekend) {
                day.setStatus("HOLIDAY");
                day.setStatusDesc("休息日");
            } else if (record != null) {
                // 设置打卡时间
                if (record.getClockInTime() != null) {
                    day.setClockInTime(record.getClockInTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                }
                if (record.getClockOutTime() != null) {
                    day.setClockOutTime(record.getClockOutTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                }

                // 综合状态判定
                day.setStatus(determineStatus(record));
                day.setStatusDesc(getStatusDesc(day.getStatus()));
            } else {
                // 工作日无记录 → 缺卡
                day.setStatus("MISSED");
                day.setStatusDesc("缺卡");
            }

            days.add(day);
        }

        AttendanceCalendarVO result = new AttendanceCalendarVO();
        result.setYearMonth(yearMonth);
        result.setDays(days);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clockIn(Long employeeId, Integer type) {
        LocalDate today = LocalDate.now();

        // 查询今日是否有记录
        MyAttendanceRecordEntity record = attendanceRecordMapper.selectOne(
                new LambdaQueryWrapper<MyAttendanceRecordEntity>()
                        .eq(MyAttendanceRecordEntity::getEmployeeId, employeeId)
                        .eq(MyAttendanceRecordEntity::getRecordDate, today)
        );

        if (record == null) {
            if (type == 1) {
                // 上班打卡 — 新建记录
                record = new MyAttendanceRecordEntity();
                record.setEmployeeId(employeeId);
                record.setRecordDate(today);
                record.setClockInTime(LocalDateTime.now());
                record.setClockInStatus("NORMAL");
                record.setCorrectionStatus("NONE");
                // 查询员工所属考勤组
                Long groupId = attendanceRecordMapper.selectDefaultAttendanceGroupId();
                if (groupId == null) {
                    throw new GlobalException(ErrorCode.BUSINESS_ERROR, "未配置考勤组，请联系管理员");
                }
                record.setGroupId(groupId);
                attendanceRecordMapper.insert(record);
            } else {
                throw new GlobalException(ErrorCode.BUSINESS_ERROR, "请先进行上班打卡");
            }
        } else {
            if (type == 1) {
                if (record.getClockInTime() != null) {
                    throw new GlobalException(ErrorCode.BUSINESS_ERROR, "今日已打卡");
                }
                record.setClockInTime(LocalDateTime.now());
                record.setClockInStatus("NORMAL");
                attendanceRecordMapper.updateById(record);
            } else {
                if (record.getClockOutTime() != null) {
                    throw new GlobalException(ErrorCode.BUSINESS_ERROR, "今日已打下班卡");
                }
                record.setClockOutTime(LocalDateTime.now());
                record.setClockOutStatus("NORMAL");
                attendanceRecordMapper.updateById(record);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMakeup(Long employeeId, MakeupRequest request) {
        // 检查当天是否已有补卡申请
        long count = correctionMapper.selectCount(
                new LambdaQueryWrapper<AttendanceCorrectionEntity>()
                        .eq(AttendanceCorrectionEntity::getEmployeeId, employeeId)
                        .eq(AttendanceCorrectionEntity::getCorrectionDate, request.getCorrectionDate())
                        .eq(AttendanceCorrectionEntity::getCorrectionType, request.getCorrectionType())
        );
        if (count > 0) {
            throw new GlobalException(ErrorCode.CONFLICT, "该日期已有同类型补卡申请");
        }

        AttendanceCorrectionEntity entity = new AttendanceCorrectionEntity();
        entity.setEmployeeId(employeeId);
        entity.setCorrectionDate(request.getCorrectionDate());
        entity.setCorrectionType(request.getCorrectionType());
        entity.setCorrectionReason(request.getCorrectionReason());
        entity.setApprovalStatus(1); // 审批中

        // 查询当天考勤记录，设置关联的 record_id
        MyAttendanceRecordEntity attendanceRecord = attendanceRecordMapper.selectOne(
                new LambdaQueryWrapper<MyAttendanceRecordEntity>()
                        .eq(MyAttendanceRecordEntity::getEmployeeId, employeeId)
                        .eq(MyAttendanceRecordEntity::getRecordDate, request.getCorrectionDate())
        );
        if (attendanceRecord != null) {
            entity.setRecordId(attendanceRecord.getId());
        }
        correctionMapper.insert(entity);
    }

    @Override
    public List<MakeupRecordVO> listMakeupRecords(Long employeeId) {
        List<AttendanceCorrectionEntity> list = correctionMapper.selectList(
                new LambdaQueryWrapper<AttendanceCorrectionEntity>()
                        .eq(AttendanceCorrectionEntity::getEmployeeId, employeeId)
                        .orderByDesc(AttendanceCorrectionEntity::getCreateTime)
        );

        return list.stream().map(e -> {
            MakeupRecordVO vo = new MakeupRecordVO();
            vo.setId(e.getId());
            vo.setCorrectionDate(e.getCorrectionDate());
            vo.setCorrectionType(e.getCorrectionType());
            vo.setCorrectionReason(e.getCorrectionReason());
            vo.setApprovalStatus(e.getApprovalStatus());
            vo.setCreateTime(e.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    // ==================== 加班申请 ====================

    @Override
    public void createOvertime(Long employeeId, OvertimeRequest request) {
        SimpleOvertimeRecord record = new SimpleOvertimeRecord();
        record.setId(overtimeIdSeq.incrementAndGet());
        record.setEmployeeId(employeeId);
        record.setOvertimeDate(request.getOvertimeDate());
        record.setDuration(request.getDuration());
        record.setReason(request.getReason());
        record.setApprovalStatus(1); // 审批中
        record.setCreateTime(LocalDateTime.now());

        overtimeStore.computeIfAbsent(employeeId, k -> new ArrayList<>()).add(record);
        log.info("员工 {} 提交加班申请，时长 {} 小时", employeeId, request.getDuration());
    }

    @Override
    public List<OvertimeRecordVO> listOvertimeRecords(Long employeeId) {
        List<SimpleOvertimeRecord> records = overtimeStore.getOrDefault(employeeId, Collections.emptyList());
        return records.stream().map(e -> {
            String statusDesc;
            switch (e.getApprovalStatus()) {
                case 0: statusDesc = "草稿"; break;
                case 1: statusDesc = "审批中"; break;
                case 2: statusDesc = "已通过"; break;
                case 3: statusDesc = "已驳回"; break;
                default: statusDesc = "未知";
            }
            return OvertimeRecordVO.builder()
                    .id(e.getId())
                    .overtimeDate(e.getOvertimeDate())
                    .duration(e.getDuration())
                    .reason(e.getReason())
                    .approvalStatus(e.getApprovalStatus())
                    .approvalStatusDesc(statusDesc)
                    .createTime(e.getCreateTime())
                    .build();
        }).sorted(Comparator.comparing(OvertimeRecordVO::getCreateTime).reversed())
          .collect(Collectors.toList());
    }

    // ==================== 考勤统计 ====================

    @Override
    public AttendanceStatisticsVO getStatistics(Long employeeId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate firstDay = ym.atDay(1);
        LocalDate lastDay = ym.atEndOfMonth();

        // 查询该月所有考勤记录
        List<MyAttendanceRecordEntity> records = attendanceRecordMapper.selectList(
                new LambdaQueryWrapper<MyAttendanceRecordEntity>()
                        .eq(MyAttendanceRecordEntity::getEmployeeId, employeeId)
                        .between(MyAttendanceRecordEntity::getRecordDate, firstDay, lastDay)
        );

        // 计算应出勤天数（周一到周五）
        long expectedDays = IntStream.rangeClosed(1, ym.lengthOfMonth())
                .mapToObj(day -> LocalDate.of(ym.getYear(), ym.getMonth(), day))
                .filter(date -> date.getDayOfWeek().getValue() <= 5)
                .count();

        // 按日期聚合考勤状态
        int actualDays = 0, lateCount = 0, earlyLeaveCount = 0, missCount = 0, leaveCount = 0;

        for (MyAttendanceRecordEntity r : records) {
            String status = determineStatus(r);
            switch (status) {
                case "NORMAL": actualDays++; break;
                case "LATE": lateCount++; break;
                case "EARLY_LEAVE": earlyLeaveCount++; break;
                case "LEAVE": leaveCount++; break;
                case "MISSED": missCount++; break;
                case "ABSENT": missCount++; break;
            }
        }

        return AttendanceStatisticsVO.builder()
                .expectedDays((int) expectedDays)
                .actualDays(actualDays + lateCount + earlyLeaveCount)
                .lateCount(lateCount)
                .earlyLeaveCount(earlyLeaveCount)
                .missCount(missCount)
                .leaveCount(leaveCount)
                .build();
    }

    /**
     * 根据上下班状态判定最终考勤状态
     */
    private String determineStatus(MyAttendanceRecordEntity record) {
        String inStatus = record.getClockInStatus();
        String outStatus = record.getClockOutStatus();

        if ("LATE".equals(inStatus) && "NORMAL".equals(outStatus)) {
            return "LATE";
        }
        if ("NORMAL".equals(inStatus) && "EARLY_LEAVE".equals(outStatus)) {
            return "EARLY_LEAVE";
        }
        if ("ABSENCE".equals(inStatus) || "ABSENCE".equals(outStatus)) {
            return "LEAVE";
        }
        if (inStatus == null && outStatus == null) {
            return "ABSENT";
        }
        if (inStatus == null || outStatus == null) {
            return "MISSED";
        }
        return "NORMAL";
    }

    private String getStatusDesc(String status) {
        return switch (status) {
            case "NORMAL" -> "正常";
            case "LATE" -> "迟到";
            case "EARLY_LEAVE" -> "早退";
            case "ABSENT" -> "旷工";
            case "MISSED" -> "缺卡";
            case "LEAVE" -> "请假";
            case "HOLIDAY" -> "休息日";
            default -> "未知";
        };
    }
}
