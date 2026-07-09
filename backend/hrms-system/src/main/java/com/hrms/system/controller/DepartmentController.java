package com.hrms.system.controller;

import com.hrms.common.model.Result;
import com.hrms.system.service.DepartmentService;
import com.hrms.system.vo.DepartmentTreeVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供部门基础配置 HTTP 接口。
 */
@RestController
@RequestMapping("/api/system/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 创建部门控制器。
     *
     * @param departmentService 部门业务服务
     */
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 查询部门树。
     *
     * @return 部门树列表
     */
    @GetMapping("/tree")
    public Result<List<DepartmentTreeVO>> listDepartmentTree() {
        return Result.success(departmentService.listDepartmentTree());
    }
}
