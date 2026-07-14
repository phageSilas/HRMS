package com.hrms.business.personnel.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.personnel.convert.TransferApplicationConvert;
import com.hrms.business.personnel.dto.TransferApplicationQueryDTO;
import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.TransferApplicationEntity;
import com.hrms.business.personnel.mapper.EmployeeSnapshotMapper;
import com.hrms.business.personnel.mapper.TransferApplicationMapper;
import com.hrms.business.personnel.service.TransferApplicationService;
import com.hrms.business.personnel.vo.TransferApplicationPageVO;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 调岗申请服务实现
 */
@Service
@RequiredArgsConstructor
public class TransferApplicationServiceImpl implements TransferApplicationService {

    private static final Long IMPOSSIBLE_EMPLOYEE_ID = -1L;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final int MAX_PAGE_SIZE = 200;

    private final TransferApplicationMapper transferApplicationMapper;

    private final EmployeeSnapshotMapper employeeSnapshotMapper;

    @Override
    public PageResult<TransferApplicationPageVO> pageTransferApplications(TransferApplicationQueryDTO queryDTO) {
        int pageNum = normalizePageNum(queryDTO.getPageNum());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        Page<TransferApplicationEntity> page = transferApplicationMapper.selectPage(
                Page.of(pageNum, pageSize),
                buildTransferApplicationWrapper(queryDTO)
        );
        Map<Long, EmployeeSnapshotEntity> employeeSnapshotMap = listEmployeeSnapshotMap(
                page.getRecords().stream().map(TransferApplicationEntity::getEmployeeId).toList()
        );
        List<TransferApplicationPageVO> records = page.getRecords().stream()
                .map(entity -> TransferApplicationConvert.toPageVO(entity, employeeSnapshotMap.get(entity.getEmployeeId())))
                .toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    /**
     * 构建调岗申请分页查询条件。
     *
     * @param queryDTO 调岗申请查询参数
     * @return 查询条件
     * 本方法使用的工具类: 无
     */
    private LambdaQueryWrapper<TransferApplicationEntity> buildTransferApplicationWrapper(TransferApplicationQueryDTO queryDTO) {
        LambdaQueryWrapper<TransferApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getDepartmentId() != null, TransferApplicationEntity::getFromDeptId, queryDTO.getDepartmentId());
        wrapper.eq(queryDTO.getApprovalStatus() != null, TransferApplicationEntity::getApprovalStatus, queryDTO.getApprovalStatus());
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            List<Long> employeeIds = listEmployeeIdsByKeyword(queryDTO.getKeyword());
            wrapper.in(CollUtil.isNotEmpty(employeeIds), TransferApplicationEntity::getEmployeeId, employeeIds);
            wrapper.eq(CollUtil.isEmpty(employeeIds), TransferApplicationEntity::getEmployeeId, IMPOSSIBLE_EMPLOYEE_ID);
        }
        wrapper.orderByDesc(TransferApplicationEntity::getCreateTime);
        return wrapper;
    }

    /**
     * 根据关键词查询员工ID列表。
     *
     * @param keyword 关键词
     * @return 员工ID列表
     * 本方法使用的工具类: StrUtil(hutool)
     */
    private List<Long> listEmployeeIdsByKeyword(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }
        // employeeService.listEmployeeIdsByKeyword(keyword); 本接口需要调用 hrms-business-employee 模块的员工关键词查询接口
        return employeeSnapshotMapper.selectList(new LambdaQueryWrapper<EmployeeSnapshotEntity>()
                        .like(EmployeeSnapshotEntity::getEmployeeName, keyword)
                        .or()
                        .like(EmployeeSnapshotEntity::getEmployeeNo, keyword))
                .stream()
                .map(EmployeeSnapshotEntity::getId)
                .toList();
    }

    /**
     * 批量查询员工快照映射。
     *
     * @param employeeIds 员工ID列表
     * @return 员工快照映射
     * 本方法使用的工具类: CollUtil(hutool)
     */
    private Map<Long, EmployeeSnapshotEntity> listEmployeeSnapshotMap(List<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        // employeeService.listEmployeeSnapshots(employeeIds); 本接口需要调用 hrms-business-employee 模块的员工快照批量查询接口
        return employeeSnapshotMapper.selectBatchIds(employeeIds).stream()
                .collect(Collectors.toMap(EmployeeSnapshotEntity::getId, Function.identity(), (left, right) -> left));
    }

    /**
     * 规范化页码。
     *
     * @param pageNum 页码
     * @return 有效页码
     * 本方法使用的工具类: 无
     */
    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    /**
     * 规范化每页条数。
     *
     * @param pageSize 每页条数
     * @return 有效每页条数
     * 本方法使用的工具类: Math(JDK)
     */
    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

}
