package com.hrms.system.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建字典类型 DTO
 */
@Data
@Schema(description = "创建字典类型请求体")
public class DictTypeCreateDTO {

    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "职位序列")
    private String dictName;

    @Schema(description = "字典类型编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "post_sequence")
    private String dictType;

    @Schema(description = "备注", example = "职位序列分类")
    private String remark;

}
