package com.hrms.system.file.service;

import com.hrms.system.file.vo.FileUploadVO;
import com.hrms.system.file.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文件服务接口。
 */
public interface FileService {

    /**
     * 上传文件。
     *
     * @param file 文件
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @return 文件上传结果
     */
    FileUploadVO upload(MultipartFile file, String bizType, Long bizId);

    /**
     * 下载文件。
     *
     * @param id 文件ID
     * @param response HTTP响应
     */
    void download(Long id, HttpServletResponse response);

    /**
     * 获取文件详情。
     *
     * @param id 文件ID
     * @return 文件详情
     */
    FileVO getById(Long id);

    /**
     * 删除文件。
     *
     * @param id 文件ID
     */
    void delete(Long id);
}