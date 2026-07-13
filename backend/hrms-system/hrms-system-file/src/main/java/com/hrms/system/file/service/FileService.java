package com.hrms.system.file.service;

/**
 * 附件管理服务接口
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param fileName 文件名
     * @param filePath 文件路径
     * @return 文件ID
     */
    Long upload(String fileName, String filePath);

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件路径
     */
    String download(Long fileId);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     */
    void delete(Long fileId);

}
