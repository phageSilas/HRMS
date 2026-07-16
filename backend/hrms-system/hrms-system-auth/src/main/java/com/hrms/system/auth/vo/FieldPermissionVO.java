package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 字段权限 VO
 */
@Data
@Schema(description = "字段权限响应对象")
public class FieldPermissionVO {

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "可查看字段列表")
    private List<String> viewableFields;

    @Schema(description = "可编辑字段列表")
    private List<String> editableFields;

    @Schema(description = "流程必填字段列表")
    private List<String> flowRequiredFields;

}
