package com.hrms.system.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典数据 VO
 */
@Data
@Schema(description = "字典数据信息")
public class DictDataVO {

    @Schema(description = "字典类型编码")
    private String dictType;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "样式属性")
    private String cssClass;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

}
