package com.hrms.system.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.system.organization.convert.PostConvert;
import com.hrms.system.organization.dto.PostCreateDTO;
import com.hrms.system.organization.dto.PostQueryDTO;
import com.hrms.system.organization.dto.PostUpdateDTO;
import com.hrms.system.organization.entity.PostDO;
import com.hrms.system.organization.mapper.PostMapper;
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.PostVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 职位服务实现类。
 */
@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    public PostServiceImpl(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Override
    public Long create(PostCreateDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getPostCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位编码不能为空");
        }

        if (!StringUtils.hasText(dto.getPostName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位名称不能为空");
        }

        // 校验职位编码是否已存在
        PostDO existPost = postMapper.selectByPostCode(dto.getPostCode());
        if (existPost != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位编码已存在");
        }

        PostDO post = PostConvert.toDO(dto);

        // 设置默认值
        if (post.getSortNo() == null) {
            post.setSortNo(0);
        }
        if (post.getStatus() == null) {
            post.setStatus(1);
        }

        postMapper.insert(post);
        return post.getId();
    }

    @Override
    public void update(PostUpdateDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位 ID 不能为空");
        }

        PostDO post = postMapper.selectById(dto.getId());
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位不存在");
        }

        PostDO updatePost = PostConvert.toDO(dto);
        postMapper.updateById(updatePost);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位 ID 不能为空");
        }

        PostDO post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位不存在");
        }

        // TODO: 检查职位是否已分配给员工

        postMapper.deleteById(id);
    }

    @Override
    public PostVO getById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位 ID 不能为空");
        }

        PostDO post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "职位不存在");
        }

        return PostConvert.toVO(post);
    }

    @Override
    public List<PostVO> list(PostQueryDTO dto) {
        LambdaQueryWrapper<PostDO> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (StringUtils.hasText(dto.getPostName())) {
                wrapper.like(PostDO::getPostName, dto.getPostName());
            }
            if (StringUtils.hasText(dto.getPostCode())) {
                wrapper.like(PostDO::getPostCode, dto.getPostCode());
            }
            if (dto.getStatus() != null) {
                wrapper.eq(PostDO::getStatus, dto.getStatus());
            }
        }
        wrapper.orderByAsc(PostDO::getSortNo);

        List<PostDO> posts = postMapper.selectList(wrapper);
        return posts.stream()
                .map(PostConvert::toVO)
                .collect(Collectors.toList());
    }
}