package com.hrms.business.personnel.controller;

import com.hrms.business.personnel.dto.EntryApplicationCreateOrUpdateRequestDTO;
import com.hrms.business.personnel.dto.EntryApplicationQueryDTO;
import com.hrms.business.personnel.service.EntryApplicationService;
import com.hrms.business.personnel.vo.EntryApplicationPageVO;
import com.hrms.business.personnel.vo.EntryApplicationSubmitVO;
import com.hrms.common.web.PageResult;
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

/**
 * 入职申请控制器
 */
@RestController
@RequestMapping("/api/v1/entry-applications")
@RequiredArgsConstructor
@Tag(name = "入职申请接口", description = "入职申请分页、草稿、审批与确认接口")
public class EntryApplicationController {

    private final EntryApplicationService entryApplicationService;

    /**
     * 分页查询入职申请列表。
     *
     * @param queryDTO 入职申请查询参数
     * @return 入职申请分页结果
     */
    @GetMapping
    @Operation(summary = "入职申请列表")
    public Result<PageResult<EntryApplicationPageVO>> pageEntryApplications(EntryApplicationQueryDTO queryDTO) {
        return Result.success(entryApplicationService.pageEntryApplications(queryDTO));
    }

    /**
     * 创建入职申请草稿。
     *
     * @param requestDTO 入职申请创建参数
     * @return 入职申请记录
     */
    @PostMapping
    @Operation(summary = "创建入职申请")
    public Result<EntryApplicationPageVO> createEntryApplication(
            @Valid @RequestBody EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        return Result.success(entryApplicationService.createEntryApplication(requestDTO));
    }

    /**
     * 更新入职申请草稿。
     *
     * @param id 入职申请ID
     * @param requestDTO 入职申请更新参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新入职申请")
    public Result<Void> updateEntryApplication(@PathVariable Long id,
                                               @Valid @RequestBody EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        entryApplicationService.updateEntryApplication(id, requestDTO);
        return Result.success();
    }

    /**
     * 提交入职申请审批。
     *
     * @param id 入职申请ID
     * @return 提交审批结果
     */
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交入职审批")
    public Result<EntryApplicationSubmitVO> submitEntryApplication(@PathVariable Long id) {
        return Result.success(entryApplicationService.submitEntryApplication(id));
    }

}
