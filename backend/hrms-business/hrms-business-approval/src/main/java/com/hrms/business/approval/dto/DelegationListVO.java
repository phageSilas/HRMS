package com.hrms.business.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 委托列表响应
 */
@Data
@Schema(description = "委托列表响应")
public class DelegationListVO {

    @Schema(description = "当前生效的委托")
    private DelegationVO activeDelegation;

    @Schema(description = "历史委托记录")
    private List<DelegationVO> records;
}
