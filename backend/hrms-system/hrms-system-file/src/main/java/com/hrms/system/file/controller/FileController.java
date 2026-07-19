package com.hrms.system.file.controller;

import com.hrms.common.web.Result;
import com.hrms.system.file.service.FileService;
import com.hrms.system.file.vo.FileDownloadVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件管理控制器。
 */
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "文件管理", description = "文件上传、下载、管理等接口")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 获取文件列表。
     */
    @GetMapping
    public Result<Object> list() {
        return Result.success();
    }

    /**
     * 下载文件。
     *
     * @param fileId 文件 ID
     * @return 文件流响应
     * @throws IOException 读取文件流异常
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) throws IOException {
        FileDownloadVO file = fileService.download(fileId);
        Path path = Path.of(file.getFilePath());
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
        String encodedName = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .contentLength(Files.size(path))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }
}
