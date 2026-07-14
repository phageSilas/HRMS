package com.hrms.system.auth.controller;

import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.Result;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.auth.vo.DataScopeVO;
import com.hrms.system.auth.vo.FieldPermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限查询控制器
 */
@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "权限查询", description = "字段权限和数据权限范围查询接口")
@RequiredArgsConstructor
public class PermissionController {

    private final RoleService roleService;

    /**
     * 获取字段权限
     */
    @GetMapping("/field")
    @Operation(summary = "获取字段权限", description = "根据业务类型获取可查看、可编辑、流程必填字段列表")
    public Result<FieldPermissionVO> getFieldPermissions(
            @RequestParam @Parameter(description = "业务类型", required = true) String bizType) {
        // TODO: 后续根据业务类型从配置或数据库查询字段权限
        // 目前返回默认字段权限（所有字段可见可编辑）
        FieldPermissionVO vo = new FieldPermissionVO();
        vo.setBizType(bizType);
        vo.setViewableFields(List.of("*"));
        vo.setEditableFields(List.of("*"));
        vo.setFlowRequiredFields(List.of());
        return Result.success(vo);
    }

    /**
     * 获取当前用户的数据权限范围
     */
    @GetMapping("/data-scope")
    @Operation(summary = "获取数据权限范围", description = "获取当前登录用户的数据权限范围")
    public Result<DataScopeVO> getDataScope() {
        Long userId = SecurityContextHolder.getUserId();
        Integer dataScope = roleService.getDataScope(userId);

        DataScopeVO vo = new DataScopeVO();
        vo.setScopeType(dataScope);
        vo.setDepartmentIds(List.of()); // TODO: 根据部门关系查询部门ID列表
        vo.setScopeDesc(getScopeDesc(dataScope));

        return Result.success(vo);
    }

    /**
     * 获取数据权限范围描述
     */
    private String getScopeDesc(Integer scopeType) {
        return switch (scopeType) {
            case 1 -> "仅本人";
            case 2 -> "本部门";
            case 3 -> "本部门及子部门";
            case 4 -> "全部";
            default -> "仅本人";
        };
    }

}
