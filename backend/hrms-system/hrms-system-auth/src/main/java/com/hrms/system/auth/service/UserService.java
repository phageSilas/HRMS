package com.hrms.system.auth.service;

import com.hrms.common.web.PageResult;
import com.hrms.system.auth.dto.UserQueryDTO;
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

}
