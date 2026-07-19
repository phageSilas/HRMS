package com.hrms.business.attendance.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.business.attendance.dto.AttendanceCalendarConfigRequestDTO;
import com.hrms.business.attendance.entity.AttendanceCalendarConfigEntity;
import com.hrms.business.attendance.mapper.AttendanceCalendarConfigMapper;
import com.hrms.business.attendance.service.AttendanceCalendarConfigService;
import com.hrms.business.attendance.vo.AttendanceCalendarConfigVO;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 考勤日历配置服务实现。
 */
@Service
@RequiredArgsConstructor
public class AttendanceCalendarConfigServiceImpl implements AttendanceCalendarConfigService {

    private static final List<Integer> DEFAULT_WORKDAYS = List.of(1, 2, 3, 4, 5);

    private final AttendanceCalendarConfigMapper attendanceCalendarConfigMapper;

    @Override
    public AttendanceCalendarConfigVO getCalendarConfig(Integer year) {
        int targetYear = normalizeYear(year);
        AttendanceCalendarConfigEntity entity = findByYear(targetYear);
        if (entity == null) {
            return buildDefaultConfig(targetYear);
        }
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceCalendarConfigVO saveCalendarConfig(AttendanceCalendarConfigRequestDTO requestDTO) {
        int targetYear = normalizeYear(requestDTO == null ? null : requestDTO.getYear());
        List<Integer> workdays = normalizeWorkdays(requestDTO == null ? null : requestDTO.getWorkdays());
        List<LocalDate> holidayDates = normalizeHolidayDates(
                targetYear,
                requestDTO == null ? null : requestDTO.getHolidayDates()
        );

        AttendanceCalendarConfigEntity entity = findByYear(targetYear);
        if (entity == null) {
            entity = new AttendanceCalendarConfigEntity();
            entity.setConfigYear(targetYear);
        }
        entity.setWorkdaysJson(JSONUtil.toJsonStr(workdays));
        entity.setHolidayDatesJson(JSONUtil.toJsonStr(
                holidayDates.stream().map(LocalDate::toString).toList()
        ));

        if (entity.getId() == null) {
            attendanceCalendarConfigMapper.insert(entity);
        } else {
            attendanceCalendarConfigMapper.updateById(entity);
        }
        return toVO(entity);
    }

    @Override
    public boolean isWorkday(LocalDate date) {
        if (date == null) {
            return false;
        }
        AttendanceCalendarConfigVO config = getCalendarConfig(date.getYear());
        if (config.getHolidayDates().contains(date)) {
            return false;
        }
        return config.getWorkdays().contains(date.getDayOfWeek().getValue());
    }

    @Override
    public List<LocalDate> listWorkdays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return List.of();
        }
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(this::isWorkday)
                .toList();
    }

    @Override
    public int countWorkdays(LocalDate startDate, LocalDate endDate) {
        return listWorkdays(startDate, endDate).size();
    }

    private AttendanceCalendarConfigEntity findByYear(Integer year) {
        return attendanceCalendarConfigMapper.selectOne(new LambdaQueryWrapper<AttendanceCalendarConfigEntity>()
                .eq(AttendanceCalendarConfigEntity::getConfigYear, year)
                .eq(AttendanceCalendarConfigEntity::getIsDeleted, 0)
                .last("limit 1"));
    }

    private AttendanceCalendarConfigVO buildDefaultConfig(int year) {
        return AttendanceCalendarConfigVO.builder()
                .year(year)
                .workdays(DEFAULT_WORKDAYS)
                .holidayDates(List.of())
                .build();
    }

    private AttendanceCalendarConfigVO toVO(AttendanceCalendarConfigEntity entity) {
        return AttendanceCalendarConfigVO.builder()
                .year(entity.getConfigYear())
                .workdays(parseWorkdays(entity.getWorkdaysJson()))
                .holidayDates(parseHolidayDates(entity.getHolidayDatesJson()))
                .build();
    }

    private int normalizeYear(Integer year) {
        if (year == null || year < 2000 || year > 2100) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "配置年份不合法");
        }
        return year;
    }

    private List<Integer> normalizeWorkdays(List<Integer> workdays) {
        if (workdays == null || workdays.isEmpty()) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "至少选择一个工作日");
        }
        List<Integer> normalized = workdays.stream()
                .filter(value -> value != null)
                .distinct()
                .sorted()
                .toList();
        if (normalized.isEmpty()) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "至少选择一个工作日");
        }
        if (normalized.stream().anyMatch(value -> value < 1 || value > 7)) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "工作日只允许使用 1 到 7");
        }
        return normalized;
    }

    private List<LocalDate> normalizeHolidayDates(int year, List<LocalDate> holidayDates) {
        if (holidayDates == null || holidayDates.isEmpty()) {
            return List.of();
        }
        Set<LocalDate> normalized = new LinkedHashSet<>();
        for (LocalDate holidayDate : holidayDates) {
            if (holidayDate == null) {
                continue;
            }
            if (holidayDate.getYear() != year) {
                throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "法定节假日必须属于同一年份");
            }
            normalized.add(holidayDate);
        }
        return normalized.stream().sorted().toList();
    }

    private List<Integer> parseWorkdays(String workdaysJson) {
        if (!JSONUtil.isTypeJSON(workdaysJson)) {
            return DEFAULT_WORKDAYS;
        }
        JSONArray array = JSONUtil.parseArray(workdaysJson);
        List<Integer> values = array.stream()
                .map(value -> value == null ? null : Integer.valueOf(String.valueOf(value)))
                .filter(value -> value != null && value >= 1 && value <= 7)
                .distinct()
                .sorted()
                .toList();
        return values.isEmpty() ? DEFAULT_WORKDAYS : values;
    }

    private List<LocalDate> parseHolidayDates(String holidayDatesJson) {
        if (!JSONUtil.isTypeJSON(holidayDatesJson)) {
            return List.of();
        }
        JSONArray array = JSONUtil.parseArray(holidayDatesJson);
        return array.stream()
                .map(value -> value == null ? null : LocalDate.parse(String.valueOf(value)))
                .filter(value -> value != null)
                .distinct()
                .sorted()
                .toList();
    }
}
