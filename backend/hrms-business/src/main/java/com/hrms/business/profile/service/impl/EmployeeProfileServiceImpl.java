package com.hrms.business.profile.service.impl;

import com.hrms.business.profile.dto.EmployeeNoGenerateRequestDTO;
import com.hrms.business.profile.dto.EmployeeQueryDTO;
import com.hrms.business.profile.service.EmployeeProfileService;
import com.hrms.business.profile.vo.EmployeeBriefVO;
import com.hrms.business.profile.vo.EmployeeNoVO;
import com.hrms.common.model.PageResult;
import org.springframework.stereotype.Service;

/**
 * 实现员工档案业务接口能力。
 */
@Service
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    /**
     * 查询员工简要信息。
     *
     * @param id 员工ID
     * @return 员工简要信息
     */
    @Override
    public EmployeeBriefVO getBriefById(Long id) {
        return new EmployeeBriefVO(id, "待维护员工", "202601001", 1L, "默认部门", "PROBATION");
    }

    /**
     * 生成员工工号。
     *
     * @param requestParam 工号生成请求参数
     * @return 工号生成结果
     */
    @Override
    public EmployeeNoVO generateEmployeeNo(EmployeeNoGenerateRequestDTO requestParam) {
        return new EmployeeNoVO("2026" + String.format("%02d", requestParam.departmentId()) + "001");
    }

    /**
     * 分页查询员工档案。
     *
     * @param queryParam 员工档案查询参数
     * @return 员工档案分页结果
     */
    @Override
    public PageResult<EmployeeBriefVO> pageEmployees(EmployeeQueryDTO queryParam) {
        return PageResult.empty(queryParam.getPageNum(), queryParam.getPageSize());
    }
}
