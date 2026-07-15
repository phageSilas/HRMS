package com.hrms.system.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位 VO
 */
@Data
@Schema(description = "职位信息")
public class PostVO {

    @Schema(description = "职位 ID")
    private Long id;

    @Schema(description = "职位名称")
    private String postName;

    @Schema(description = "职位编码")
    private String postCode;

    @Schema(description = "职位序列：M-管理序列 P-专业序列 S-支持序列")
    private String sequenceCode;

    @Schema(description = "序列名称")
    private String sequenceName;

    @Schema(description = "所属部门 ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "职级下限")
    private String jobLevelMin;

    @Schema(description = "职级上限")
    private String jobLevelMax;

    @Schema(description = "默认试用期（月）")
    private Integer defaultProbationMonth;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
