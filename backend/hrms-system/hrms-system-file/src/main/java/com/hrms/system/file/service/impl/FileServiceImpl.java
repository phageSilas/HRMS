package com.hrms.system.file.service.impl;

import cn.hutool.core.util.StrUtil;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.system.file.entity.FileEntity;
import com.hrms.system.file.mapper.FileMapper;
import com.hrms.system.file.service.FileService;
import com.hrms.system.file.vo.FileDownloadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;

    @Override
    public Long upload(String fileName,
                       String filePath,
                       Long fileSize,
                       String fileType,
                       String mimeType,
                       String businessType,
                       Long businessId) {
        if (StrUtil.isBlank(fileName) || StrUtil.isBlank(filePath)) {
            throw new GlobalException(ErrorCode.PARAM_REQUIRED, "文件名和文件路径不能为空");
        }
        Path path = Path.of(filePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new GlobalException(ErrorCode.FILE_UPLOAD_ERROR, "待登记文件不存在");
        }

        FileEntity entity = new FileEntity();
        entity.setFileName(fileName);
        entity.setFilePath(path.toAbsolutePath().toString());
        entity.setFileSize(resolveFileSize(path, fileSize));
        entity.setFileType(fileType);
        entity.setMimeType(StrUtil.blankToDefault(mimeType, "application/octet-stream"));
        entity.setMd5(calculateMd5(path));
        entity.setBusinessType(businessType);
        entity.setBusinessId(businessId);
        fileMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public FileDownloadVO download(Long fileId) {
        FileEntity entity = fileMapper.selectById(fileId);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        Path path = Path.of(entity.getFilePath());
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new GlobalException(ErrorCode.FILE_DOWNLOAD_ERROR, "文件不存在或已损坏");
        }
        return FileDownloadVO.builder()
                .fileId(entity.getId())
                .fileName(entity.getFileName())
                .filePath(path.toAbsolutePath().toString())
                .mimeType(StrUtil.blankToDefault(entity.getMimeType(), "application/octet-stream"))
                .build();
    }

    @Override
    public void delete(Long fileId) {
        fileMapper.deleteById(fileId);
    }

    private Long resolveFileSize(Path path, Long fileSize) {
        if (fileSize != null && fileSize > 0) {
            return fileSize;
        }
        try {
            return Files.size(path);
        } catch (IOException ex) {
            throw new GlobalException(ErrorCode.FILE_UPLOAD_ERROR, "读取文件大小失败");
        }
    }

    private String calculateMd5(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try (InputStream inputStream = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, length);
                }
            }
            byte[] hash = digest.digest();
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new GlobalException(ErrorCode.FILE_UPLOAD_ERROR, "计算文件摘要失败");
        }
    }
}
