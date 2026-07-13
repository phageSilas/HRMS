package com.hrms.business.personnel.controller;

import com.hrms.common.web.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入转调离控制器
 */
@RestController
@RequestMapping("/api/v1/personnel")
@Tag(name = "入转调离接口", description = "入职、转正、调岗、离职等流程相关接口")
public class PersonnelController {

    /**
     * 获取审批列表
     */
    @GetMapping("/approval-list")
    public Result<Object> getApprovalList() {
        return Result.success();
    }

}
