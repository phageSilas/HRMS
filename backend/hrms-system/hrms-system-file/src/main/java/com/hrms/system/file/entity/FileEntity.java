package com.hrms.system.file.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件实体。
 */
@Data
@TableName("sys_file")
public class FileEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名。
     */
    private String fileName;

    /**
     * 存储路径。
     */
    private String filePath;

    /**
     * 文件大小（字节）。
     */
    private Long fileSize;

    /**
     * 文件类型。
     */
    private String fileType;

    /**
     * MIME 类型。
     */
    private String mimeType;

    /**
     * 文件 MD5。
     */
    private String md5;

    /**
     * 业务类型。
     */
    private String businessType;

    /**
     * 业务 ID。
     */
    private Long businessId;

    /**
     * 创建人。
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除。
     */
    @TableLogic
    private Integer isDeleted;
}
