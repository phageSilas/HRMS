package com.hrms.business.salary.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪资账套项目实体，对齐 hr_salary_template_item 表；本表没有 version 字段，不能继承 BaseEntity。
 */
@Data
@TableName("hr_salary_template_item")
public class SalaryTemplateItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private String itemCode;

    private String itemName;

    private String category;

    private String calcRule;

    private BigDecimal defaultValue;

    private Integer sortNo;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
