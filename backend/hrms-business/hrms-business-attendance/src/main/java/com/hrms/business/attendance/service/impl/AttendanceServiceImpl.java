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
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.entity.AttendanceGroupEntity;
import com.hrms.business.attendance.entity.AttendanceRecordEntity;
import com.hrms.business.attendance.entity.EmployeeSnapshotEntity;
import com.hrms.business.attendance.enums.ClockPeriodEnum;
import com.hrms.business.attendance.mapper.AttendanceGroupMapper;
import com.hrms.business.attendance.mapper.AttendanceRecordMapper;
import com.hrms.business.attendance.mapper.EmployeeSnapshotMapper;
import com.hrms.business.attendance.mq.AttendanceClockCreatedEvent;
import com.hrms.business.attendance.mq.AttendanceClockEventHandler;
import com.hrms.business.attendance.mq.AttendanceMqConstants;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceClockVO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

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

    private final AttendanceGroupMapper attendanceGroupMapper;

    private final AttendanceRecordMapper attendanceRecordMapper;

    private final EmployeeSnapshotMapper employeeSnapshotMapper;

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
