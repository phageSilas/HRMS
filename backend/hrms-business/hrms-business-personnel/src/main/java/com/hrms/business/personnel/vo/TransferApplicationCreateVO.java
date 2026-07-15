package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调岗申请创建 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "调岗申请创建结果")
public class TransferApplicationCreateVO {

    /**
     * 调岗申请ID
     */
    @Schema(description = "调岗申请ID")
    private Long id;

    /**
     * 审批状态
     */
    @Schema(description = "审批状态")
    private Integer approvalStatus;

}
