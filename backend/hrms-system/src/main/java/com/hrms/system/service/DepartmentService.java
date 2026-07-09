package com.hrms.system.service;

import com.hrms.system.vo.DepartmentTreeVO;
import java.util.List;

/**
 * 定义部门基础配置业务接口能力。
 */
public interface DepartmentService {

    /**
     * 查询部门树。
     *
     * @return 部门树列表
     */
    List<DepartmentTreeVO> listDepartmentTree();
}
