package com.hrms.system.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建字典数据 DTO
 */
@Data
@Schema(description = "创建字典数据请求体")
public class DictDataCreateDTO {

    @Schema(description = "字典类型编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "post_sequence")
    private String dictType;

    @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED, example = "管理序列")
    private String dictLabel;

    @Schema(description = "字典值", requiredMode = Schema.RequiredMode.REQUIRED, example = "M")
    private String dictValue;

    @Schema(description = "排序号", example = "1")
    private Integer sort;

    @Schema(description = "样式属性", example = "primary")
    private String cssClass;

    @Schema(description = "备注", example = "管理序列")
    private String remark;

}
