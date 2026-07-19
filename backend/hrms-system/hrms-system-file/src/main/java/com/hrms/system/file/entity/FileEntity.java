package com.hrms.system.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class FileEntity extends BaseEntity {

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
}
