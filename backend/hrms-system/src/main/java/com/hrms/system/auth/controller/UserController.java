package com.hrms.system.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.web.Result;
import com.hrms.system.auth.dto.UserCreateDTO;
import com.hrms.system.auth.dto.UserQueryDTO;
import com.hrms.system.auth.dto.UserUpdateDTO;
import com.hrms.system.auth.service.UserService;
import com.hrms.system.auth.vo.UserVO;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器。
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 创建用户。
     *
     * @param dto 用户创建请求
     * @return 用户 ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody UserCreateDTO dto) {
        Long id = userService.create(dto);
        return Result.success(id);
    }

    /**
     * 更新用户。
     *
     * @param id  用户 ID
     * @param dto 用户更新请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        dto.setId(id);
        userService.update(dto);
        return Result.success();
    }

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    /**
     * 查询用户详情。
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        UserVO vo = userService.getById(id);
        return Result.success(vo);
    }

    /**
     * 分页查询用户列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<UserVO>> list(UserQueryDTO dto) {
        Page<UserVO> page = userService.list(dto);
        return Result.success(page);
    }

    /**
     * 重置用户密码。
     *
     * @param id          用户 ID
     * @param newPassword 新密码
     * @return 成功响应
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
}