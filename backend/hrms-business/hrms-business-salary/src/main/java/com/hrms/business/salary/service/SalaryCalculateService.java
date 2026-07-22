package com.hrms.business.salary.service;

import com.hrms.business.salary.dto.SalaryBatchAdjustmentRequestDTO;
import com.hrms.business.salary.dto.SalaryBatchCreateRequestDTO;
import com.hrms.business.salary.dto.SalaryBatchItemQueryDTO;
import com.hrms.business.salary.mq.event.SalaryBatchCalculateMessage;
import com.hrms.business.salary.vo.SalaryBatchExportVO;
import com.hrms.business.salary.vo.SalaryBatchItemVO;
import com.hrms.business.salary.vo.SalaryBatchPreviewVO;
import com.hrms.business.salary.vo.SalaryBatchTrendVO;
import com.hrms.business.salary.vo.SalaryBatchVO;

import com.hrms.common.web.PageResult;
import java.util.List;

/**
 * 薪资核算服务接口。
 */
public interface SalaryCalculateService {

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
     * @return 当前薪资批次，未找到时返回null
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
     * 处理薪资批次核算消息。
     *
     * @param message 薪资批次核算消息
     * 本方法使用的工具类: 无
     */
    void handleBatchCalculateMessage(SalaryBatchCalculateMessage message);

    /**
     * 预览薪资批次。
     *
     * @param batchId 批次ID
     * @return 批次预览
     * 本方法使用的工具类: 无
     */
    SalaryBatchPreviewVO previewBatch(Long batchId);

    /**
     * 分页查询薪资批次明细（前10页走Redis缓存，超出走游标分页）。
     *
     * @param batchId  批次ID
     * @param queryDTO 分页查询参数
     * @return 分页结果
     * 本方法使用的工具类：PageResult(hrms-common)
     */
    PageResult<SalaryBatchItemVO> pageBatchItems(Long batchId, SalaryBatchItemQueryDTO queryDTO);

    /**
     * 导出薪资批次 Excel。
     *
     * @param batchId 批次 ID
     * @return 导出结果
     * 本方法使用的工具类: 无
     */
    SalaryBatchExportVO exportBatch(Long batchId);

    /**
     * 提交薪资批次审批。
     *
     * @param batchId 批次ID
     * @return 提交后的批次
     * 本方法使用的工具类: 无
     */
    SalaryBatchVO submitBatch(Long batchId);
}
