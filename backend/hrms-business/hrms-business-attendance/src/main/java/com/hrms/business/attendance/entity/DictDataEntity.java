package com.hrms.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据只读实体，对应 sys_dict_data。
 */
@Data
@TableName("sys_dict_data")
public class DictDataEntity {

    /**
     * 主键ID。
     */
    @TableId
    private Long id;

    /**
     * 字典类型编码。
     */
    private String dictType;

    /**
     * 字典标签。
     */
    private String dictLabel;

    /**
     * 字典值。
     */
    private String dictValue;

    /**
     * 排序号。
     */
    private Integer sort;

    /**
     * 状态：1-启用，0-禁用。
     */
    private Integer status;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 是否删除。
     */
    private Integer isDeleted;
}
