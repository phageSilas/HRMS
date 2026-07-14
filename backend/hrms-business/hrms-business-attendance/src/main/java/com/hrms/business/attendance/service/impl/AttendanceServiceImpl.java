package com.hrms.business.attendance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.attendance.cache.AttendanceCacheKeys;
import com.hrms.business.attendance.convert.AttendanceGroupConvert;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
import com.hrms.business.attendance.dto.AttendanceGroupQueryDTO;
import com.hrms.business.attendance.entity.AttendanceGroupEntity;
import com.hrms.business.attendance.mapper.AttendanceGroupMapper;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 考勤管理服务实现。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceGroupMapper attendanceGroupMapper;

    private final StringRedisTemplate stringRedisTemplate;

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
