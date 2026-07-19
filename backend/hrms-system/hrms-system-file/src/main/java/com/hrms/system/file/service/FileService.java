package com.hrms.system.file.service;

import com.hrms.system.file.vo.FileDownloadVO;

/**
 * 文件管理服务接口。
 */
public interface FileService {

    /**
     * 登记文件元数据。
     *
     * @param fileName     原始文件名
     * @param filePath     存储路径
     * @param fileSize     文件大小
     * @param fileType     文件类型
     * @param mimeType     MIME 类型
     * @param businessType 业务类型
     * @param businessId   业务 ID
     * @return 文件 ID
     */
    Long upload(String fileName,
                String filePath,
                Long fileSize,
                String fileType,
                String mimeType,
                String businessType,
                Long businessId);

    /**
     * 查询下载文件信息。
     *
     * @param fileId 文件 ID
     * @return 文件下载信息
     */
    FileDownloadVO download(Long fileId);

    /**
     * 删除文件记录。
     *
     * @param fileId 文件 ID
     */
    void delete(Long fileId);
}
