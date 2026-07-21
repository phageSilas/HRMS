package com.hrms.system.organization.controller;

import com.hrms.common.web.PageResult;
import com.hrms.common.web.Result;
import com.hrms.system.organization.dto.PostCreateDTO;
import com.hrms.system.organization.dto.PostQueryDTO;
import com.hrms.system.organization.dto.PostUpdateDTO;
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.PostVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 职位管理控制器
 */
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "职位管理", description = "职位的增删改查接口")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 职位分页列表
     */
    @GetMapping
    @Operation(summary = "职位列表查询", description = "分页查询职位列表，支持按部门、序列筛选和关键词搜索")
    public Result<PageResult<PostVO>> list(PostQueryDTO queryDTO) {
        PageResult<PostVO> pageResult = postService.listPosts(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 职位详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "职位详情查询", description = "根据ID查询职位详情")
    public Result<PostVO> getById(@PathVariable Long id) {
        PostVO post = postService.getPostById(id);
        return Result.success(post);
    }

    /**
     * 创建职位
     */
    @PostMapping
    @Operation(summary = "创建职位", description = "创建新职位，支持M/P/S三种序列")
    public Result<Long> create(@Valid @RequestBody PostCreateDTO createDTO) {
        Long id = postService.createPost(createDTO);
        return Result.success(id);
    }

    /**
     * 更新职位
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新职位", description = "根据ID更新职位信息")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PostUpdateDTO updateDTO) {
        postService.updatePost(id, updateDTO);
        return Result.success();
    }

    /**
     * 删除职位
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除职位", description = "根据ID逻辑删除职位")
    public Result<Void> delete(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success();
    }

    /**
     * 统计各序列职位数量
     */
    @GetMapping("/stats/sequence")
    @Operation(summary = "职位序列统计", description = "统计各序列(M/P/S)的职位数量")
    public Result<java.util.Map<String, Long>> countBySequence() {
        java.util.Map<String, Long> stats = postService.countBySequence();
        return Result.success(stats);
    }

}
