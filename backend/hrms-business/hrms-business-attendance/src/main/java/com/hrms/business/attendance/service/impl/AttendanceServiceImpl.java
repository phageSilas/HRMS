package com.hrms.business.attendance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.attendance.convert.AttendanceGroupConvert;
import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.entity.AttendanceGroupEntity;
import com.hrms.business.attendance.mapper.AttendanceGroupMapper;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 考勤管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceGroupMapper attendanceGroupMapper;

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
