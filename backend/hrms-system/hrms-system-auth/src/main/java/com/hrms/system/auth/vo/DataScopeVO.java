package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 数据权限范围 VO
 */
@Data
@Schema(description = "数据权限范围响应对象")
public class DataScopeVO {

    @Schema(description = "权限范围类型：1-仅本人 2-本部门 3-本部门及子部门 4-全部")
    private Integer scopeType;

    @Schema(description = "部门ID列表（当scopeType为2或3时有效）")
    private List<Long> departmentIds;

    @Schema(description = "权限范围描述")
    private String scopeDesc;

}
