package com.hrms.business.salary.service;

import com.hrms.business.salary.dto.SalaryManagePayslipQueryDTO;
import com.hrms.business.salary.dto.SalaryManagePayslipVerifyRequestDTO;
import com.hrms.business.salary.dto.SalaryPayslipPageQueryDTO;
import com.hrms.business.salary.dto.SalaryPayslipVerifyRequestDTO;
import com.hrms.business.salary.vo.SalaryManagePayslipPageVO;
import com.hrms.business.salary.vo.SalaryPayslipDetailVO;
import com.hrms.business.salary.vo.SalaryPayslipListVO;
import com.hrms.business.salary.vo.SalaryPayslipVerifyVO;
import com.hrms.business.salary.vo.SalaryTrendVO;
import com.hrms.common.web.PageResult;

import java.util.List;

/**
 * 工资条服务接口。
 */
public interface PaySlipService {

    /**
     * 查询当前员工工资条列表。
     *
     * @param month 薪资月份，可为空
     * @return 工资条列表
     * 本方法使用的工具类: List(JDK)
     */
    List<SalaryPayslipListVO> listPayslips(String month);

    /**
     * 分页查询当前员工工资条列表。
     *
     * @param queryDTO 查询参数
     * @return 工资条分页结果
     * 本方法使用的工具类: PageResult(hrms-common)
     */
    PageResult<SalaryPayslipListVO> pagePayslips(SalaryPayslipPageQueryDTO queryDTO);

    /**
     * 工资条二次验证。
     *
     * @param requestDTO 验证请求
     * @return 验证结果
     * 本方法使用的工具类: 无
     */
    SalaryPayslipVerifyVO verifyPayslip(SalaryPayslipVerifyRequestDTO requestDTO);

    /**
     * 管理端工资条二次验证。
     *
     * @param requestDTO 验证请求
     * @return 验证结果
     * 本方法使用的工具类: 无
     */
    SalaryPayslipVerifyVO verifyManagePayslip(SalaryManagePayslipVerifyRequestDTO requestDTO);

    /**
     * 分页查询管理端工资条列表。
     *
     * @param queryDTO 查询参数
     * @return 工资条分页结果
     * 本方法使用的工具类: PageResult(hrms-common)
     */
    PageResult<SalaryManagePayslipPageVO> pageManagePayslips(SalaryManagePayslipQueryDTO queryDTO);

    /**
     * 查询工资条详情。
     *
     * @param payslipId 工资条ID
     * @return 工资条详情
     * 本方法使用的工具类: 无
     */
    SalaryPayslipDetailVO getPayslipDetail(Long payslipId);

    /**
     * 查询管理端工资条详情。
     *
     * @param payslipId 工资条ID
     * @return 工资条详情
     * 本方法使用的工具类: 无
     */
    SalaryPayslipDetailVO getManagePayslipDetail(Long payslipId);

    /**
     * 查询当前员工近 6 个月薪资趋势。
     *
     * @return 薪资趋势
     * 本方法使用的工具类: List(JDK)
     */
    List<SalaryTrendVO> getTrend();
}
