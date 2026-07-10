package com.hrms.system.file.service.impl;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.system.file.config.FileUploadConfig;
import com.hrms.system.file.convert.FileConvert;
import com.hrms.system.file.entity.FileDO;
import com.hrms.system.file.mapper.FileMapper;
import com.hrms.system.file.service.FileService;
import com.hrms.system.file.vo.FileUploadVO;
import com.hrms.system.file.vo.FileVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;
    private final FileUploadConfig config;

    public FileServiceImpl(FileMapper fileMapper, FileUploadConfig config) {
        this.fileMapper = fileMapper;
        this.config = config;
    }

    @Override
    public FileUploadVO upload(MultipartFile file, String bizType, Long bizId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        long fileSize = file.getSize();
        String contentType = file.getContentType();

        if (fileSize > config.getMaxSize()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                "文件大小超过限制，最大允许 " + (config.getMaxSize() / 1024 / 1024) + "MB");
        }

        String extension = getFileExtension(originalFilename);
        if (!isAllowedType(extension)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的文件类型");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String relativePath = datePath + "/" + fileName;
        String fullPath = config.getBasePath() + "/" + relativePath;

        try {
            File destFile = new File(fullPath);
            destFile.getParentFile().mkdirs();
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
        }

        FileDO fileDO = new FileDO();
        fileDO.setFileName(originalFilename);
        fileDO.setFilePath(relativePath);
        fileDO.setFileSize(fileSize);
        fileDO.setFileType(contentType);
        fileDO.setFileExtension(extension);
        fileDO.setBizType(bizType);
        fileDO.setBizId(bizId);
        fileDO.setDownloadCount(0);
        fileDO.setStatus(1);

        fileMapper.insert(fileDO);
        return FileConvert.toUploadVO(fileDO);
    }

    @Override
    public void download(Long id, HttpServletResponse response) {
        FileDO fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不存在");
        }

        String fullPath = config.getBasePath() + "/" + fileDO.getFilePath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不存在");
        }

        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileDO.getFileName(), StandardCharsets.UTF_8));

            FileInputStream fis = new FileInputStream(file);
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            fis.close();
            os.flush();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件下载失败");
        }

        fileDO.setDownloadCount(fileDO.getDownloadCount() + 1);
        fileMapper.updateById(fileDO);
    }

    @Override
    public FileVO getById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件ID不能为空");
        }
        FileDO fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不存在");
        }
        return FileConvert.toVO(fileDO);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件ID不能为空");
        }
        FileDO fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不存在");
        }
        fileMapper.deleteById(id);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    private boolean isAllowedType(String extension) {
        String[] allowed = config.getAllowedTypes().split(",");
        return Arrays.asList(allowed).contains(extension.toLowerCase());
    }
}