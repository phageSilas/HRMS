package com.hrms.system.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.DataScopeUtils;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.PageResult;
import com.hrms.system.organization.dto.PostCreateDTO;
import com.hrms.system.organization.dto.PostQueryDTO;
import com.hrms.system.organization.dto.PostUpdateDTO;
import com.hrms.system.organization.entity.DeptEntity;
import com.hrms.system.organization.entity.PostEntity;
import com.hrms.system.organization.mapper.DeptMapper;
import com.hrms.system.organization.mapper.PostMapper;
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 职位服务实现
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final DeptMapper deptMapper;

    @Override
    public PageResult<PostVO> listPosts(PostQueryDTO queryDTO) {
        LambdaQueryWrapper<PostEntity> wrapper = Wrappers.lambdaQuery();

        // 数据权限过滤
        int dataScope = DataScopeUtils.getCurrentUserDataScope();
        Long userDeptId = SecurityContextHolder.getDeptId();
        Set<Long> visibleDeptIds = getVisibleDeptIds(dataScope, userDeptId);

        if (visibleDeptIds != null) {
            // 非全部权限，需要按部门过滤
            if (visibleDeptIds.isEmpty()) {
                // 仅本人权限，返回空结果（职位没有 create_by 字段，无法按创建人过滤）
                return PageResult.of(Collections.emptyList(), 0, queryDTO.getPageNum(), queryDTO.getPageSize());
            }
            wrapper.in(PostEntity::getDeptId, visibleDeptIds);
        }

        // 按部门筛选
        if (queryDTO.getDeptId() != null) {
            wrapper.eq(PostEntity::getDeptId, queryDTO.getDeptId());
        }

        // 按序列筛选
        if (StringUtils.hasText(queryDTO.getSequenceCode())) {
            wrapper.eq(PostEntity::getSequenceCode, queryDTO.getSequenceCode());
        }

        // 关键词搜索（职位名称或编码）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(PostEntity::getPostName, queryDTO.getKeyword())
                    .or()
                    .like(PostEntity::getPostCode, queryDTO.getKeyword()));
        }

        wrapper.eq(PostEntity::getStatus, 1)
                .orderByAsc(PostEntity::getSortNo);

        Page<PostEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<PostEntity> resultPage = postMapper.selectPage(page, wrapper);

        List<PostVO> records = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(records, resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize());
    }

    @Override
    public PostVO getPostById(Long id) {
        PostEntity post = postMapper.selectById(id);
        if (post == null || post.getIsDeleted() == 1) {
            throw new GlobalException(ErrorCode.NOT_FOUND);
        }
        return convertToVO(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostCreateDTO createDTO) {
        // 校验职位编码唯一性
        Long count = postMapper.selectCount(
                Wrappers.<PostEntity>lambdaQuery()
                        .eq(PostEntity::getPostCode, createDTO.getPostCode())
        );
        if (count > 0) {
            throw new GlobalException(ErrorCode.DATA_DUPLICATE);
        }

        PostEntity post = new PostEntity();
        post.setPostName(createDTO.getPostName());
        post.setPostCode(createDTO.getPostCode());
        post.setSequenceCode(createDTO.getSequenceCode());
        post.setDeptId(createDTO.getDeptId());
        post.setJobLevelMin(createDTO.getJobLevelMin());
        post.setJobLevelMax(createDTO.getJobLevelMax());
        post.setDefaultProbationMonth(createDTO.getDefaultProbationMonth());
        post.setDescription(createDTO.getDescription());
        post.setSortNo(createDTO.getSortNo());
        post.setStatus(1);

        postMapper.insert(post);
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long id, PostUpdateDTO updateDTO) {
        PostEntity post = postMapper.selectById(id);
        if (post == null || post.getIsDeleted() == 1) {
            throw new GlobalException(ErrorCode.NOT_FOUND);
        }

        post.setPostName(updateDTO.getPostName());
        post.setSequenceCode(updateDTO.getSequenceCode());
        post.setDeptId(updateDTO.getDeptId());
        post.setJobLevelMin(updateDTO.getJobLevelMin());
        post.setJobLevelMax(updateDTO.getJobLevelMax());
        post.setDefaultProbationMonth(updateDTO.getDefaultProbationMonth());
        post.setDescription(updateDTO.getDescription());
        post.setSortNo(updateDTO.getSortNo());

        postMapper.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id) {
        PostEntity post = postMapper.selectById(id);
        if (post == null || post.getIsDeleted() == 1) {
            throw new GlobalException(ErrorCode.NOT_FOUND);
        }

        // 注意：职位下是否有在职员工的校验由调用方（前端/客户端）负责
        // 原因：
        // 1. organization模块不能反向依赖employee模块（避免循环依赖）
        // 2. 前端应在删除职位前先调用 /api/v1/employees/check-post?postId={id} 接口校验
        // 3. 如果该校验接口返回 true（有在职员工），前端应阻止删除操作并提示用户
        // 4. 后端此处不重复校验员工（避免跨模块耦合）

        postMapper.deleteById(id);
    }

    /**
     * 转换为 VO
     */
    private PostVO convertToVO(PostEntity post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setPostName(post.getPostName());
        vo.setPostCode(post.getPostCode());
        vo.setSequenceCode(post.getSequenceCode());
        vo.setDeptId(post.getDeptId());
        vo.setJobLevelMin(post.getJobLevelMin());
        vo.setJobLevelMax(post.getJobLevelMax());
        vo.setDefaultProbationMonth(post.getDefaultProbationMonth());
        vo.setStatus(post.getStatus());
        vo.setSortNo(post.getSortNo());
        vo.setCreateTime(post.getCreateTime());

        // 设置序列名称
        vo.setSequenceName(getSequenceName(post.getSequenceCode()));

        // 设置部门名称
        if (post.getDeptId() != null) {
            DeptEntity dept = deptMapper.selectById(post.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }

        return vo;
    }

    /**
     * 获取序列名称
     */
    private String getSequenceName(String sequenceCode) {
        if (sequenceCode == null) {
            return null;
        }
        return switch (sequenceCode) {
            case "M" -> "管理序列";
            case "P" -> "专业序列";
            case "S" -> "支持序列";
            default -> sequenceCode;
        };
    }

    @Override
    public java.util.Map<String, Long> countBySequence() {
        java.util.Map<String, Long> result = new java.util.HashMap<>();

        // 初始化三个序列的计数
        result.put("M", 0L);
        result.put("P", 0L);
        result.put("S", 0L);

        // 数据权限过滤
        int dataScope = DataScopeUtils.getCurrentUserDataScope();
        Long userDeptId = SecurityContextHolder.getDeptId();
        Set<Long> visibleDeptIds = getVisibleDeptIds(dataScope, userDeptId);

        // 查询各序列职位数量（只统计启用的职位）
        LambdaQueryWrapper<PostEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PostEntity::getStatus, 1);

        if (visibleDeptIds != null) {
            if (visibleDeptIds.isEmpty()) {
                return result; // 仅本人权限，返回空统计
            }
            wrapper.in(PostEntity::getDeptId, visibleDeptIds);
        }

        List<PostEntity> posts = postMapper.selectList(wrapper);

        // 统计各序列数量
        for (PostEntity post : posts) {
            String sequenceCode = post.getSequenceCode();
            if (sequenceCode != null && result.containsKey(sequenceCode)) {
                result.put(sequenceCode, result.get(sequenceCode) + 1);
            }
        }

        return result;
    }

    /**
     * 获取用户可见的部门ID集合
     *
     * @param dataScope  数据权限范围
     * @param userDeptId 用户所属部门ID
     * @return 可见的部门ID集合，null 表示全部权限
     */
    private Set<Long> getVisibleDeptIds(int dataScope, Long userDeptId) {
        if (dataScope == DataScopeUtils.DATA_SCOPE_ALL) {
            // 全部权限
            return null;
        }

        if (userDeptId == null) {
            // 没有部门信息，返回空集合
            return Collections.emptySet();
        }

        if (dataScope == DataScopeUtils.DATA_SCOPE_SELF || dataScope == DataScopeUtils.DATA_SCOPE_DEPT) {
            // 仅本人或本部门：只返回用户所属部门
            return Collections.singleton(userDeptId);
        }

        if (dataScope == DataScopeUtils.DATA_SCOPE_DEPT_AND_SUB) {
            // 本部门及下属：返回用户所属部门及其所有子部门
            List<DeptEntity> allDepts = deptMapper.selectList(
                    Wrappers.<DeptEntity>lambdaQuery()
                            .eq(DeptEntity::getStatus, 1)
                            .eq(DeptEntity::getIsDeleted, 0)
            );
            return getSubDeptIds(userDeptId, allDepts).stream().collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

    /**
     * 递归获取子部门ID列表（含自身）
     */
    private List<Long> getSubDeptIds(Long deptId, List<DeptEntity> allDepts) {
        List<Long> result = new java.util.ArrayList<>();
        result.add(deptId);

        List<DeptEntity> children = allDepts.stream()
                .filter(dept -> deptId.equals(dept.getParentId()))
                .collect(Collectors.toList());

        for (DeptEntity child : children) {
            result.addAll(getSubDeptIds(child.getId(), allDepts));
        }

        return result;
    }

}
