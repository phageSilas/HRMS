package com.hrms.business.personnel.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 离职申请分页查询 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "离职申请分页查询")
public class LeaveApplicationQueryDTO {

    /**
     * 关键词，匹配员工姓名或工号
     */
    @Schema(description = "关键词，匹配员工姓名或工号")
    private String keyword;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long departmentId;

    /**
     * 离职类型：resign / terminate / mutual / contract_end
     */
    @Schema(description = "离职类型：resign / terminate / mutual / contract_end")
    private String leaveType;

    /**
     * 审批状态
     */
    @JsonAlias("status")
    @Schema(description = "审批状态")
    private Integer approvalStatus;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    @Builder.Default
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "20")
    @Builder.Default
    private Integer pageSize = 20;

}
