package com.hrms.system.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建职位 DTO
 */
@Data
@Schema(description = "创建职位请求体")
public class PostCreateDTO {

    @Schema(description = "职位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java开发工程师")
    private String postName;

    @Schema(description = "职位编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "JAVA_DEV")
    private String postCode;

    @Schema(description = "职位序列：M-管理序列 P-专业序列 S-支持序列", requiredMode = Schema.RequiredMode.REQUIRED, example = "P")
    private String sequenceCode;

    @Schema(description = "所属部门 ID", example = "1")
    private Long deptId;

    @Schema(description = "职级下限", example = "P3")
    private String jobLevelMin;

    @Schema(description = "职级上限", example = "P7")
    private String jobLevelMax;

    @Schema(description = "默认试用期（月）", example = "3")
    private Integer defaultProbationMonth;

    @Schema(description = "职位描述", example = "负责后端开发")
    private String description;

    @Schema(description = "排序号", example = "1")
    private Integer sortNo;

}
