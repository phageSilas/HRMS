package com.hrms.system.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型 VO
 */
@Data
@Schema(description = "字典类型信息")
public class DictTypeVO {

    @Schema(description = "字典类型 ID")
    private Long id;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "字典类型编码")
    private String dictType;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
