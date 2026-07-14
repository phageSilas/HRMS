package com.hrms.system.auth.controller;

import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import com.hrms.system.auth.dto.UserCreateDTO;
import com.hrms.system.auth.dto.UserQueryDTO;
import com.hrms.system.auth.dto.UserUpdateDTO;
import com.hrms.system.auth.service.UserService;
import com.hrms.system.auth.vo.ResetPasswordVO;
import com.hrms.system.auth.vo.UserCreateResultVO;
import com.hrms.system.auth.vo.UserDetailVO;
import com.hrms.system.auth.vo.UserListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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

    /**
     * 用户详情查询
     */
    @GetMapping("/{id}")
    @Operation(summary = "用户详情查询", description = "根据ID查询用户详情")
    public Result<UserDetailVO> getById(@PathVariable Long id) {
        UserDetailVO userDetail = userService.getUserDetail(id);
        return Result.success(userDetail);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户并关联角色")
    public Result<UserCreateResultVO> create(@Valid @RequestBody UserCreateDTO createDTO) {
        UserCreateResultVO result = userService.createUser(createDTO);
        return Result.success(result);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "根据ID更新用户信息")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO updateDTO) {
        userService.updateUser(id, updateDTO);
        return Result.success();
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据ID逻辑删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-pwd")
    @Operation(summary = "重置用户密码", description = "重置用户密码并返回新密码")
    public Result<ResetPasswordVO> resetPassword(@PathVariable Long id) {
        ResetPasswordVO result = userService.resetPassword(id);
        return Result.success(result);
    }

}
