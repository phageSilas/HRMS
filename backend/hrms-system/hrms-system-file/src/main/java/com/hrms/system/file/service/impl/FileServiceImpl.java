package com.hrms.system.file.service.impl;

import com.hrms.system.file.service.FileService;
import org.springframework.stereotype.Service;

/**
 * 附件管理服务实现
 */
@Service
public class FileServiceImpl implements FileService {

    @Override
    public Long upload(String fileName, String filePath) {
        // TODO: 实现文件上传逻辑
        return null;
    }

    @Override
    public String download(Long fileId) {
        // TODO: 实现文件下载逻辑
        return null;
    }

    @Override
    public void delete(Long fileId) {
        // TODO: 实现文件删除逻辑
    }

}
