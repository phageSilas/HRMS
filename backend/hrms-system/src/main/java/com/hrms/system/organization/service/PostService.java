package com.hrms.system.organization.service;

import com.hrms.system.organization.dto.PostCreateDTO;
import com.hrms.system.organization.dto.PostQueryDTO;
import com.hrms.system.organization.dto.PostUpdateDTO;
import com.hrms.system.organization.vo.PostVO;

import java.util.List;

/**
 * 职位服务接口。
 */
public interface PostService {

    /**
     * 创建职位。
     *
     * @param dto 职位创建请求
     * @return 职位 ID
     */
    Long create(PostCreateDTO dto);

    /**
     * 更新职位。
     *
     * @param dto 职位更新请求
     */
    void update(PostUpdateDTO dto);

    /**
     * 删除职位。
     *
     * @param id 职位 ID
     */
    void delete(Long id);

    /**
     * 查询职位详情。
     *
     * @param id 职位 ID
     * @return 职位详情
     */
    PostVO getById(Long id);

    /**
     * 查询职位列表。
     *
     * @param dto 查询条件
     * @return 职位列表
     */
    List<PostVO> list(PostQueryDTO dto);
}