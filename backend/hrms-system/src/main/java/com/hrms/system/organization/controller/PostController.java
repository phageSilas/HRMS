package com.hrms.system.organization.controller;

import com.hrms.common.web.Result;
import com.hrms.system.organization.dto.PostCreateDTO;
import com.hrms.system.organization.dto.PostQueryDTO;
import com.hrms.system.organization.dto.PostUpdateDTO;
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.PostVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位管理控制器。
 */
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 创建职位。
     */
    @PostMapping
    public Result<Long> create(@RequestBody PostCreateDTO dto) {
        Long id = postService.create(dto);
        return Result.success(id);
    }

    /**
     * 更新职位。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PostUpdateDTO dto) {
        dto.setId(id);
        postService.update(dto);
        return Result.success();
    }

    /**
     * 删除职位。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return Result.success();
    }

    /**
     * 查询职位详情。
     */
    @GetMapping("/{id}")
    public Result<PostVO> getById(@PathVariable Long id) {
        PostVO vo = postService.getById(id);
        return Result.success(vo);
    }

    /**
     * 查询职位列表。
     */
    @GetMapping
    public Result<List<PostVO>> list(PostQueryDTO dto) {
        List<PostVO> list = postService.list(dto);
        return Result.success(list);
    }
}