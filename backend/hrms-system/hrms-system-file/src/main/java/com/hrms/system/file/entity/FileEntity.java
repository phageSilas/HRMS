package com.hrms.system.file.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

/**
 * 附件实体
 */
@Data
public class FileEntity extends BaseEntity {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 关联业务类型
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 状态：1-正常，0-删除
     */
    private Integer status;

}
