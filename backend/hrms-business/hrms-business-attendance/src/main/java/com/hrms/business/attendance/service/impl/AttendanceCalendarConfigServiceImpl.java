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
import java.util.Objects;
import java.util.Set;

import static com.hrms.business.attendance.common.constant.AttendanceCalendarConstant.DEFAULT_WORKDAYS;

/**
 * 考勤日历配置服务实现。
 */
@Service
@RequiredArgsConstructor
public class AttendanceCalendarConfigServiceImpl implements AttendanceCalendarConfigService {


    // 考勤日历配置Mapper
    private final AttendanceCalendarConfigMapper attendanceCalendarConfigMapper;

    /**
     * 获取考勤日历配置。
     * @param year 配置年份
     * @return 考勤日历配置
     */
    @Override
    public AttendanceCalendarConfigVO getCalendarConfig(Integer year) {
        int targetYear = normalizeYear(year);
        AttendanceCalendarConfigEntity entity = findByYear(targetYear);
        if (entity == null) {
            return buildDefaultConfig(targetYear);
        }
        return toVO(entity);
    }

    /**
     * 保存考勤日历配置。
     * @param requestDTO 考勤日历配置请求DTO
     * @return 考勤日历配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceCalendarConfigVO saveCalendarConfig(AttendanceCalendarConfigRequestDTO requestDTO) {
        int targetYear = normalizeYear(requestDTO == null ? null : requestDTO.getYear());
        List<Integer> workdays = normalizeWorkdays(requestDTO.getWorkdays());
        List<LocalDate> holidayDates = normalizeHolidayDates(
                targetYear,
                requestDTO.getHolidayDates()
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

    /**
     * 判断指定日期是否为工作日。
     * @param date 指定日期
     * @return 是否为工作日
     */
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

    /**
     * 列出指定日期范围内的所有工作日。
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作日列表
     */
    @Override
    public List<LocalDate> listWorkdays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return List.of();
        }
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(this::isWorkday)
                .toList();
    }

    /**
     * 计算指定日期范围内的工作日数量。
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作日数量
     */
    @Override
    public int countWorkdays(LocalDate startDate, LocalDate endDate) {
        return listWorkdays(startDate, endDate).size();
    }

    /**
     * 根据年份查询考勤日历配置。
     * @param year 年份
     * @return 考勤日历配置
     */
    private AttendanceCalendarConfigEntity findByYear(Integer year) {
        return attendanceCalendarConfigMapper.selectOne(new LambdaQueryWrapper<AttendanceCalendarConfigEntity>()
                .eq(AttendanceCalendarConfigEntity::getConfigYear, year)
                .eq(AttendanceCalendarConfigEntity::getIsDeleted, 0)
                .last("limit 1"));
    }

    /**
     * 构建默认的考勤日历配置。
     * @param year 年份
     * @return 考勤日历配置
     */
    private AttendanceCalendarConfigVO buildDefaultConfig(int year) {
        return AttendanceCalendarConfigVO.builder()
                .year(year)
                .workdays(DEFAULT_WORKDAYS)
                .holidayDates(List.of())
                .build();
    }

    /**
     * 将实体对象转换为VO对象。
     * @param entity 实体对象
     * @return VO对象
     */
    private AttendanceCalendarConfigVO toVO(AttendanceCalendarConfigEntity entity) {
        return AttendanceCalendarConfigVO.builder()
                .year(entity.getConfigYear())
                .workdays(parseWorkdays(entity.getWorkdaysJson()))
                .holidayDates(parseHolidayDates(entity.getHolidayDatesJson()))
                .build();
    }

    /**
     * 规范化年份。
     * @param year 年份
     * @return 规范化后的年份
     */
    private int normalizeYear(Integer year) {
        if (year == null || year < 2000 || year > 2100) {
            throw new GlobalException(ErrorCode.PARAM_FORMAT_ERROR, "配置年份不合法");
        }
        return year;
    }

    /**
     * 规范化工作日列表。
     * @param workdays 工作日列表
     * @return 规范化后的工作日列表
     */
    private List<Integer> normalizeWorkdays(List<Integer> workdays) {
        if (workdays == null || workdays.isEmpty()) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "至少选择一个工作日");
        }
        List<Integer> normalized = workdays.stream()
                .filter(Objects::nonNull)
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

    /**
     * 规范化法定节假日列表。
     * @param year 年份
     * @param holidayDates 法定节假日列表
     * @return 规范化后的法定节假日列表
     */
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

    /**
     * 解析工作日列表。
     * @param workdaysJson 工作日JSON字符串
     * @return 工作日列表
     */
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

    /**
     * 解析法定节假日列表。
     * @param holidayDatesJson 法定节假日JSON字符串
     * @return 法定节假日列表
     */
    private List<LocalDate> parseHolidayDates(String holidayDatesJson) {
        if (!JSONUtil.isTypeJSON(holidayDatesJson)) {
            return List.of();
        }
        JSONArray array = JSONUtil.parseArray(holidayDatesJson);
        return array.stream()
                .map(value -> value == null ? null : LocalDate.parse(String.valueOf(value)))
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }
}
