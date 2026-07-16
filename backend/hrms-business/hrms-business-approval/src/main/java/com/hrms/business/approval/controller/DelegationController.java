package com.hrms.business.approval.controller;

import com.hrms.business.approval.dto.DelegationCreateRequest;
import com.hrms.business.approval.dto.DelegationListVO;
import com.hrms.business.approval.service.DelegationService;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 委托审批控制器
 * <p>
 * API-APR-07/08/09
 * </p>
 */
@RestController
@RequestMapping("/api/v1/approval/delegation")
@Tag(name = "委托审批", description = "委托设置、取消、查询")
@RequiredArgsConstructor
public class DelegationController {

    private final DelegationService delegationService;

    @Operation(summary = "设置委托")
    @PostMapping
    public Result<Map<String, Long>> createDelegation(@Valid @RequestBody DelegationCreateRequest request) {
        Long userId = SecurityContextHolder.getUserId();
        Long id = delegationService.createDelegation(userId, request);
        return Result.success(Map.of("id", id));
    }

    @Operation(summary = "取消委托")
    @PutMapping("/{id}/cancel")
    public Result<Map<String, Boolean>> cancelDelegation(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getUserId();
        delegationService.cancelDelegation(id, userId);
        return Result.success(Map.of("success", true));
    }

    @Operation(summary = "查询我的委托")
    @GetMapping("/my")
    public Result<DelegationListVO> getMyDelegations() {
        Long userId = SecurityContextHolder.getUserId();
        return Result.success(delegationService.findMyDelegations(userId));
    }
}
