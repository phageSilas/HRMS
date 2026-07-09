package com.hrms.system.service.impl;

import com.hrms.system.service.DepartmentService;
import com.hrms.system.vo.DepartmentTreeVO;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 实现部门基础配置业务接口能力。
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    /**
     * 查询部门树。
     *
     * @return 部门树列表
     */
    @Override
    public List<DepartmentTreeVO> listDepartmentTree() {
        return List.of(new DepartmentTreeVO(1L, "默认部门", 0L, List.of()));
    }
}
