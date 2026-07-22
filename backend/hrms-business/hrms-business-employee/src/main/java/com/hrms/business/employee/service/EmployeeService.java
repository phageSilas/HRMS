package com.hrms.business.employee.service;

import com.hrms.business.employee.dto.EmployeeApprovalSyncUpdateDTO;
import com.hrms.business.employee.dto.EmployeeCreateDTO;
import com.hrms.business.employee.dto.EmployeeQueryDTO;
import com.hrms.business.employee.dto.EmployeeUpdateDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.vo.EmployeeDetailVO;
import com.hrms.business.employee.vo.EmployeeGenNoVO;
import com.hrms.business.employee.vo.EmployeeListVO;
import com.hrms.common.web.PageResult;

import java.util.List;

/**
 * 员工服务接口
 */
public interface EmployeeService {

    /**
     * 分页查询员工列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<EmployeeListVO> listEmployees(EmployeeQueryDTO queryDTO);

    /**
     * 获取员工详情
     *
     * @param id 员工ID
     * @return 员工详情
     */
    EmployeeDetailVO getEmployeeDetail(Long id);

    /**
     * 新增员工
     *
     * @param createDTO 创建参数
     * @return 创建后的员工简要信息
     */
    EmployeeEntity createEmployee(EmployeeCreateDTO createDTO);

    /**
     * 全量更新员工
     *
     * @param id        员工ID
     * @param updateDTO 更新参数
     * @return 更新后的员工
     */
    EmployeeEntity updateEmployee(Long id, EmployeeUpdateDTO updateDTO);

    /**
     * 部分更新员工
     *
     * @param id        员工ID
     * @param updateDTO 更新参数
     * @return 更新后的员工
     */
    EmployeeEntity patchEmployee(Long id, EmployeeUpdateDTO updateDTO);

    /**
     * 删除员工（逻辑删除）
     *
     * @param id 员工ID
     */
    void deleteEmployee(Long id);

    /**
     * 生成员工工号
     *
     * @param deptCode 部门编码
     * @return 工号信息
     */
    EmployeeGenNoVO generateEmployeeNo(String deptCode);

    /**
     * 获取员工简要信息（模块间服务）
     *
     * @param id 员工ID
     * @return 员工实体
     */
    EmployeeEntity getEmployeeBrief(Long id);

    /**
     * 按部门获取员工列表（模块间服务）
     *
     * @param deptId 部门ID
     * @return 员工列表
     */
    List<EmployeeEntity> getEmployeesByDept(Long deptId);

    /**
     * 按职位获取在职员工列表（模块间服务）
     *
     * @param postId 职位ID
     * @return 在职员工列表
     */
    List<EmployeeEntity> getEmployeesByPostId(Long postId);

    /**
     * 检查部门下是否有在职员工（模块间服务）
     *
     * @param deptId 部门ID
     * @return true-有员工，false-无员工
     */
    boolean hasEmployeesInDept(Long deptId);

    /**
     * 检查职位下是否有在职员工（模块间服务）
     *
     * @param postId 职位ID
     * @return true-有员工，false-无员工
     */

    boolean hasEmployeesInPost(Long postId);
    /**
     * 批量更新员工的部门ID（模块间服务）
     * 用于部门合并场景，将源部门的所有员工迁移到目标部门
     *
     * @param oldDeptId 原部门ID
     * @param newDeptId 新部门ID
     * @return 更新的员工数量
     */
    int updateDeptIdByDeptId(Long oldDeptId, Long newDeptId);

    /**
     * 审批通过后同步员工档案字段。
     * <p>
     * 供人员模块等跨模块审批回调使用，仅按参数中非空字段执行更新。
     * </p>
     *
     * @param employeeId 员工ID
     * @param updateDTO 审批联动更新参数
     * @return 更新后的员工实体
     */
    EmployeeEntity syncEmployeeForApproval(Long employeeId, EmployeeApprovalSyncUpdateDTO updateDTO);

}
