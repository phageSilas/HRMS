package com.hrms.business.salary.service;

import com.hrms.business.salary.dto.EmployeeSalaryProfileRequestDTO;
import com.hrms.business.salary.dto.SalaryBatchAdjustmentRequestDTO;
import com.hrms.business.salary.dto.SalaryBatchCreateRequestDTO;
import com.hrms.business.salary.dto.SalaryPayslipVerifyRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateCreateOrUpdateRequestDTO;
import com.hrms.business.salary.dto.SalaryTemplateQueryDTO;
import com.hrms.business.salary.vo.EmployeeSalaryProfileVO;
import com.hrms.business.salary.vo.SalaryBatchItemVO;
import com.hrms.business.salary.vo.SalaryBatchPreviewVO;
import com.hrms.business.salary.vo.SalaryBatchTrendVO;
import com.hrms.business.salary.vo.SalaryBatchVO;
import com.hrms.business.salary.vo.SalaryPayslipDetailVO;
import com.hrms.business.salary.vo.SalaryPayslipListVO;
import com.hrms.business.salary.vo.SalaryPayslipVerifyVO;
import com.hrms.business.salary.vo.SalaryTemplatePageVO;
import com.hrms.business.salary.vo.SalaryTrendVO;
import com.hrms.common.web.PageResult;

import java.util.List;

/**
 * 薪资管理服务接口。
 */
public interface SalaryService {

    /**
     * 分页查询薪资账套。
     *
     * @param queryDTO 查询参数
     * @return 薪资账套分页结果
     * 本方法使用的工具类: PageResult(hrms-common)
     */
    PageResult<SalaryTemplatePageVO> pageTemplates(SalaryTemplateQueryDTO queryDTO);

    /**
     * 创建薪资账套。
     *
     * @param requestDTO 创建请求
     * @return 创建后的薪资账套
     * 本方法使用的工具类: 无
     */
    SalaryTemplatePageVO createTemplate(SalaryTemplateCreateOrUpdateRequestDTO requestDTO);

    /**
     * 更新薪资账套。
     *
     * @param id         账套ID
     * @param requestDTO 更新请求
     * @return 更新后的薪资账套
     * 本方法使用的工具类: 无
     */
    SalaryTemplatePageVO updateTemplate(Long id, SalaryTemplateCreateOrUpdateRequestDTO requestDTO);

    /**
     * 查询员工薪资档案。
     *
     * @param employeeId 员工ID
     * @return 薪资档案
     * 本方法使用的工具类: 无
     */
    EmployeeSalaryProfileVO getEmployeeProfile(Long employeeId);

    /**
     * 设置员工薪资档案。
     *
     * @param employeeId 员工ID
     * @param requestDTO 设置请求
     * @return 设置后的薪资档案
     * 本方法使用的工具类: 无
     */
    EmployeeSalaryProfileVO setEmployeeProfile(Long employeeId, EmployeeSalaryProfileRequestDTO requestDTO);

    /**
     * 创建薪资核算批次。
     *
     * @param requestDTO 创建请求
     * @return 薪资批次
     * 本方法使用的工具类: 无
     */
    SalaryBatchVO createBatch(SalaryBatchCreateRequestDTO requestDTO);

    /**
     * 按月份和核算范围查询当前薪资批次。
     *
     * @param salaryMonth 薪资月份
     * @param scopeType   核算范围类型
     * @param scopeValue  核算范围值
     * @return 当前薪资批次，未找到时返回 null
     * 本方法使用的工具类: 无
     */
    SalaryBatchVO getCurrentBatch(String salaryMonth, String scopeType, String scopeValue);

    /**
     * 查询管理端跨月份薪资趋势。
     *
     * @param anchorMonth 统计截止月份
     * @param months      向前统计月数
     * @param scopeType   核算范围类型
     * @param scopeValue  核算范围值
     * @return 薪资趋势列表
     * 本方法使用的工具类: List(JDK)
     */
    List<SalaryBatchTrendVO> listBatchTrend(String anchorMonth, Integer months, String scopeType, String scopeValue);

    /**
     * 保存薪资批次人工调整。
     *
     * @param batchId    薪资批次ID
     * @param requestDTO 人工调整请求
     * @return 调整后的员工薪资明细
     * 本方法使用的工具类: 无
     */
    SalaryBatchItemVO saveBatchAdjustments(Long batchId, SalaryBatchAdjustmentRequestDTO requestDTO);

    /**
     * 重新计算薪资批次。
     *
     * @param batchId 薪资批次ID
     * @return 重新计算后的薪资批次
     * 本方法使用的工具类: 无
     */
    SalaryBatchVO recalculateBatch(Long batchId);

    /**
     * 触发薪资核算。
     *
     * @param batchId 批次ID
     * @return 核算后的批次
     * 本方法使用的工具类: 无
     */
    SalaryBatchVO calculateBatch(Long batchId);

    /**
     * 预览薪资批次。
     *
     * @param batchId 批次ID
     * @return 批次预览
     * 本方法使用的工具类: 无
     */
    SalaryBatchPreviewVO previewBatch(Long batchId);

    /**
     * 提交薪资批次审批。
     *
     * @param batchId 批次ID
     * @return 提交后的批次
     * 本方法使用的工具类: 无
     */
    SalaryBatchVO submitBatch(Long batchId);

    /**
     * 查询当前员工工资条列表。
     *
     * @param month 薪资月份，可为空
     * @return 工资条列表
     * 本方法使用的工具类: List(JDK)
     */
    List<SalaryPayslipListVO> listPayslips(String month);

    /**
     * 工资条二次验证。
     *
     * @param requestDTO 验证请求
     * @return 验证结果
     * 本方法使用的工具类: 无
     */
    SalaryPayslipVerifyVO verifyPayslip(SalaryPayslipVerifyRequestDTO requestDTO);

    /**
     * 查询工资条详情。
     *
     * @param payslipId 工资条ID
     * @return 工资条详情
     * 本方法使用的工具类: 无
     */
    SalaryPayslipDetailVO getPayslipDetail(Long payslipId);

    /**
     * 查询当前员工近 6 个月薪资趋势。
     *
     * @return 薪资趋势
     * 本方法使用的工具类: List(JDK)
     */
    List<SalaryTrendVO> getTrend();
}
