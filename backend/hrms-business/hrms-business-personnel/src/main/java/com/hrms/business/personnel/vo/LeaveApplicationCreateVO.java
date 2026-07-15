package com.hrms.business.personnel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 离职申请创建 VO
 */
@Data
@Schema(description = "离职申请创建结果")
public class LeaveApplicationCreateVO {

    /**
     * 离职申请ID
     */
    @Schema(description = "离职申请ID")
    private Long id;

}
