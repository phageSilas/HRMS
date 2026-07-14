package com.hrms.system.auth.service;

import com.hrms.common.web.PageResult;
import com.hrms.system.auth.dto.UserQueryDTO;
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

}
