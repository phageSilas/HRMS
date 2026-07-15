package com.hrms.system.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职位实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_post")
public class PostEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 职位名称
     */
    private String postName;

    /**
     * 职位编码
     */
    private String postCode;

    /**
     * 职位序列：M-管理序列 P-专业序列 S-支持序列
     */
    private String sequenceCode;

    /**
     * 所属部门 ID，NULL=全公司通用
     */
    private Long deptId;

    /**
     * 职级下限（如 P3）
     */
    private String jobLevelMin;

    /**
     * 职级上限（如 P7）
     */
    private String jobLevelMax;

    /**
     * 默认试用期（月）
     */
    private Integer defaultProbationMonth;

    /**
     * 职位描述
     */
    private String description;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 备注
     */
    private String remark;

}
