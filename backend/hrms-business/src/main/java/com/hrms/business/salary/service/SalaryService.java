package com.hrms.business.salary.service;

import com.hrms.business.salary.dto.SalaryMonthlyQueryDTO;
import com.hrms.business.salary.vo.SalaryMonthlyVO;
import com.hrms.common.model.PageResult;

/**
 * 定义薪资业务接口能力。
 */
public interface SalaryService {

    /**
     * 分页查询月度薪资。
     *
     * @param queryParam 月度薪资查询参数
     * @return 月度薪资分页结果
     */
    PageResult<SalaryMonthlyVO> pageMonthlySalary(SalaryMonthlyQueryDTO queryParam);

    /**
     * 查询月度薪资详情。
     *
     * @param id 月度薪资ID
     * @return 月度薪资详情
     */
    SalaryMonthlyVO getMonthlySalary(Long id);
}
