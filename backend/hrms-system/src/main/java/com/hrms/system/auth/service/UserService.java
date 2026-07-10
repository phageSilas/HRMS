package com.hrms.system.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.auth.dto.UserCreateDTO;
import com.hrms.system.auth.dto.UserQueryDTO;
import com.hrms.system.auth.dto.UserUpdateDTO;
import com.hrms.system.auth.vo.UserVO;

/**
 * 用户服务接口。
 */
public interface UserService {

    /**
     * 创建用户。
     *
     * @param dto 用户创建请求
     * @return 用户 ID
     */
    Long create(UserCreateDTO dto);

    /**
     * 更新用户。
     *
     * @param dto 用户更新请求
     */
    void update(UserUpdateDTO dto);

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     */
    void delete(Long id);

    /**
     * 查询用户详情。
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    UserVO getById(Long id);

    /**
     * 分页查询用户列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    Page<UserVO> list(UserQueryDTO dto);

    /**
     * 重置用户密码。
     *
     * @param id          用户 ID
     * @param newPassword 新密码
     */
    void resetPassword(Long id, String newPassword);
}