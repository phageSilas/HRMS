package com.hrms.system.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 文件实体。
 *
 * <p>对应数据库表 sys_file，存储文件元数据。</p>
 */
@TableName("sys_file")
public class FileDO extends BaseEntity {

    /**
     * 文件名称（原始文件名）。
     */
    private String fileName;

    /**
     * 存储路径（相对路径）。
     */
    private String filePath;

    /**
     * 文件大小（字节）。
     */
    private Long fileSize;

    /**
     * 文件类型（MIME 类型）。
     */
    private String fileType;

    /**
     * 文件扩展名。
     */
    private String fileExtension;

    /**
     * 业务类型（如：employee、contract）。
     */
    private String bizType;

    /**
     * 业务 ID。
     */
    private Long bizId;

    /**
     * 下载次数。
     */
    private Integer downloadCount;

    /**
     * 状态：1正常 0删除。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}