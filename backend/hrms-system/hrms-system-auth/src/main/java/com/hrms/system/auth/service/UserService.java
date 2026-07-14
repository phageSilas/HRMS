package com.hrms.system.auth.service;

import com.hrms.common.web.PageResult;
import com.hrms.system.auth.dto.UserCreateDTO;
import com.hrms.system.auth.dto.UserQueryDTO;
import com.hrms.system.auth.dto.UserUpdateDTO;
import com.hrms.system.auth.vo.ResetPasswordVO;
import com.hrms.system.auth.vo.UserCreateResultVO;
import com.hrms.system.auth.vo.UserDetailVO;
import com.hrms.system.auth.vo.UserListVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<UserListVO> listUsers(UserQueryDTO queryDTO);

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserDetailVO getUserDetail(Long id);

    /**
     * 创建用户
     *
     * @param createDTO 创建参数
     * @return 创建结果
     */
    UserCreateResultVO createUser(UserCreateDTO createDTO);

    /**
     * 更新用户
     *
     * @param id        用户ID
     * @param updateDTO 更新参数
     */
    void updateUser(Long id, UserUpdateDTO updateDTO);

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @return 重置结果
     */
    ResetPasswordVO resetPassword(Long id);

}
