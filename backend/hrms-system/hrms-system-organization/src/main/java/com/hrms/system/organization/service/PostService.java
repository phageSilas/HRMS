package com.hrms.system.organization.service;

import com.hrms.common.web.PageResult;
import com.hrms.system.organization.dto.PostCreateDTO;
import com.hrms.system.organization.dto.PostQueryDTO;
import com.hrms.system.organization.dto.PostUpdateDTO;
import com.hrms.system.organization.entity.PostEntity;
import com.hrms.system.organization.vo.PostVO;

/**
 * 职位服务接口
 */
public interface PostService {

    /**
     * 分页查询职位列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<PostVO> listPosts(PostQueryDTO queryDTO);

    /**
     * 根据 ID 查询职位详情
     *
     * @param id 职位 ID
     * @return 职位详情
     */
    PostVO getPostById(Long id);

    /**
     * 创建职位
     *
     * @param createDTO 创建职位 DTO
     * @return 创建的职位 ID
     */
    Long createPost(PostCreateDTO createDTO);

    /**
     * 更新职位
     *
     * @param id        职位 ID
     * @param updateDTO 更新职位 DTO
     */
    void updatePost(Long id, PostUpdateDTO updateDTO);

    /**
     * 删除职位（逻辑删除）
     *
     * @param id 职位 ID
     */
    void deletePost(Long id);

}
