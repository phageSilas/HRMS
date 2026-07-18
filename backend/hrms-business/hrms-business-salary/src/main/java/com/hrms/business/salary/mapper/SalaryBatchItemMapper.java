package com.hrms.business.salary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.salary.dto.SalaryManagePayslipQueryDTO;
import com.hrms.business.salary.entity.SalaryBatchItemEntity;
import com.hrms.business.salary.vo.SalaryManagePayslipPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * 薪资核算明细 Mapper。
 */
@Mapper
public interface SalaryBatchItemMapper extends BaseMapper<SalaryBatchItemEntity> {

    /**
     * 分页查询管理端工资条列表。
     *
     * @param page            分页参数
     * @param query           查询参数
     * @param visibleStatuses 员工端可见的批次状态
     * @return 工资条分页记录
     * 本方法使用的工具类: Page(MyBatis-Plus)
     */
    @Select("""
            <script>
            SELECT
                item.id AS id,
                item.batch_id AS batchId,
                item.employee_id AS employeeId,
                employee.employee_name AS employeeName,
                employee.employee_no AS employeeNo,
                employee.dept_id AS deptId,
                batch.salary_month AS salaryMonth,
                item.gross_salary AS grossSalary,
                item.deduction_total AS deductionTotal,
                item.net_salary AS netSalary,
                batch.batch_status AS batchStatus,
                CASE
                    WHEN batch.batch_status IN
                    <foreach collection="visibleStatuses" item="status" open="(" separator="," close=")">
                        #{status}
                    </foreach>
                    THEN 'PUBLISHED'
                    ELSE 'UNPUBLISHED'
                END AS publishStatus,
                CASE
                    WHEN batch.batch_status NOT IN
                    <foreach collection="visibleStatuses" item="status" open="(" separator="," close=")">
                        #{status}
                    </foreach>
                    THEN 'UNPUBLISHED'
                    WHEN view_record.id IS NULL THEN 'UNVIEWED'
                    ELSE 'VIEWED'
                END AS viewStatus
            FROM hr_salary_batch_item item
            INNER JOIN hr_salary_batch batch ON batch.id = item.batch_id AND batch.is_deleted = 0
            INNER JOIN hr_employee employee ON employee.id = item.employee_id AND employee.is_deleted = 0
            LEFT JOIN hr_salary_payslip_view_record view_record
                ON view_record.payslip_item_id = item.id AND view_record.is_deleted = 0
            <where>
                <if test="query.month != null and query.month != ''">
                    AND batch.salary_month = #{query.month}
                </if>
                <if test="query.keyword != null and query.keyword != ''">
                    AND (employee.employee_name LIKE CONCAT('%', #{query.keyword}, '%')
                        OR employee.employee_no LIKE CONCAT('%', #{query.keyword}, '%'))
                </if>
                <if test="query.deptId != null">
                    AND employee.dept_id = #{query.deptId}
                </if>
                <if test="query.viewStatus != null and query.viewStatus != ''">
                    <choose>
                        <when test="query.viewStatus == 'UNPUBLISHED'">
                            AND batch.batch_status NOT IN
                            <foreach collection="visibleStatuses" item="status" open="(" separator="," close=")">
                                #{status}
                            </foreach>
                        </when>
                        <when test="query.viewStatus == 'VIEWED'">
                            AND batch.batch_status IN
                            <foreach collection="visibleStatuses" item="status" open="(" separator="," close=")">
                                #{status}
                            </foreach>
                            AND view_record.id IS NOT NULL
                        </when>
                        <when test="query.viewStatus == 'UNVIEWED'">
                            AND batch.batch_status IN
                            <foreach collection="visibleStatuses" item="status" open="(" separator="," close=")">
                                #{status}
                            </foreach>
                            AND view_record.id IS NULL
                        </when>
                    </choose>
                </if>
            </where>
            ORDER BY batch.salary_month DESC, item.id DESC
            </script>
            """)
    Page<SalaryManagePayslipPageVO> selectManagePayslipPage(Page<SalaryManagePayslipPageVO> page,
                                                            @Param("query") SalaryManagePayslipQueryDTO query,
                                                            @Param("visibleStatuses") Set<String> visibleStatuses);
}
