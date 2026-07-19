package com.hrms.business.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 委托视图对象
 */
@Data
@Schema(description = "委托信息")
public class DelegationVO {

    @Schema(description = "委托ID")
    private Long id;

    @Schema(description = "被委托人姓名")
    private String delegateeName;

    @Schema(description = "生效时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "委托原因")
    private String reason;

    @Schema(description = "状态：active / expired / cancelled")
    private String status;

    @Schema(description = "被委托人职位名称")
    private String position;
}
