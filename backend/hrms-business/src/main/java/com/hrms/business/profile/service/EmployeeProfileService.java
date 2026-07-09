package com.hrms.business.profile.service;

import com.hrms.business.profile.dto.EmployeeNoGenerateRequestDTO;
import com.hrms.business.profile.dto.EmployeeQueryDTO;
import com.hrms.business.profile.vo.EmployeeBriefVO;
import com.hrms.business.profile.vo.EmployeeNoVO;
import com.hrms.common.model.PageResult;

/**
 * 定义员工档案业务接口能力。
 */
public interface EmployeeProfileService {

    /**
     * 查询员工简要信息。
     *
     * @param id 员工ID
     * @return 员工简要信息
     */
    EmployeeBriefVO getBriefById(Long id);

    /**
     * 生成员工工号。
     *
     * @param requestParam 工号生成请求参数
     * @return 工号生成结果
     */
    EmployeeNoVO generateEmployeeNo(EmployeeNoGenerateRequestDTO requestParam);

    /**
     * 分页查询员工档案。
     *
     * @param queryParam 员工档案查询参数
     * @return 员工档案分页结果
     */
    PageResult<EmployeeBriefVO> pageEmployees(EmployeeQueryDTO queryParam);
}
