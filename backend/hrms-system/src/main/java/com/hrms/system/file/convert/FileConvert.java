package com.hrms.system.file.convert;

import com.hrms.system.file.entity.FileDO;
import com.hrms.system.file.vo.FileUploadVO;
import com.hrms.system.file.vo.FileVO;

/**
 * 文件对象转换器。
 */
public class FileConvert {

    public static FileUploadVO toUploadVO(FileDO entity) {
        if (entity == null) return null;
        FileUploadVO vo = new FileUploadVO();
        vo.setId(entity.getId());
        vo.setFileName(entity.getFileName());
        vo.setFilePath(entity.getFilePath());
        vo.setFileSize(entity.getFileSize());
        vo.setFileType(entity.getFileType());
        vo.setUploadTime(entity.getCreateTime());
        return vo;
    }

    public static FileVO toVO(FileDO entity) {
        if (entity == null) return null;
        FileVO vo = new FileVO();
        vo.setId(entity.getId());
        vo.setFileName(entity.getFileName());
        vo.setFilePath(entity.getFilePath());
        vo.setFileSize(entity.getFileSize());
        vo.setFileType(entity.getFileType());
        vo.setFileExtension(entity.getFileExtension());
        vo.setBizType(entity.getBizType());
        vo.setBizId(entity.getBizId());
        vo.setDownloadCount(entity.getDownloadCount());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}