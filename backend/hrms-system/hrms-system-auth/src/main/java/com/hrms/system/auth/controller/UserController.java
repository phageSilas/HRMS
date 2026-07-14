package com.hrms.system.auth.controller;

import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import com.hrms.system.auth.dto.UserQueryDTO;
import com.hrms.system.auth.service.UserService;
import com.hrms.system.auth.vo.UserListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户的增删改查接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户列表查询（分页）
     */
    @GetMapping
    @Operation(summary = "用户列表查询", description = "分页查询用户列表，支持按条件筛选")
    public Result<PageResult<UserListVO>> list(UserQueryDTO queryDTO) {
        PageResult<UserListVO> pageResult = userService.listUsers(queryDTO);
        return Result.success(pageResult);
    }

}
