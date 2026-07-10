package com.hrms.system.file.controller;

import com.hrms.common.web.Result;
import com.hrms.system.file.service.FileService;
import com.hrms.system.file.vo.FileUploadVO;
import com.hrms.system.file.vo.FileVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件管理控制器。
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 文件上传。
     */
    @PostMapping("/upload")
    public Result<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "bizId", required = false) Long bizId) {
        FileUploadVO vo = fileService.upload(file, bizType, bizId);
        return Result.success(vo);
    }

    /**
     * 文件下载。
     */
    @GetMapping("/{id}/download")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        fileService.download(id, response);
    }

    /**
     * 文件预览。
     *
     * <p>支持图片、PDF 等可直接预览的文件类型。
     * 返回文件字节流，浏览器可直接显示。</p>
     *
     * @param id 文件 ID
     * @return 文件字节流
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id) {
        try {
            // 1. 获取文件信息
            FileVO fileVO = fileService.getById(id);
            if (fileVO == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. 安全检查：验证文件路径是否在允许的目录内
            Path filePath = Paths.get(fileVO.getFilePath()).normalize().toAbsolutePath();

            // 防止路径遍历攻击：检查是否包含 ".."
            if (fileVO.getFilePath().contains("..")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            // 3. 检查是否为常规文件（防止目录访问）
            if (!Files.isRegularFile(filePath)) {
                return ResponseEntity.badRequest().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);

            // 4. 确定 Content-Type
            String contentType = determineContentType(fileVO.getFileName());

            // 5. 设置响应头（inline 表示浏览器直接显示）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileContent.length);

            // 对文件名进行 URL 编码
            String encodedFileName = URLEncoder.encode(fileVO.getFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            headers.add("Content-Disposition", "inline; filename=\"" + encodedFileName + "\"");

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件名确定 Content-Type。
     *
     * @param fileName 文件名
     * @return Content-Type
     */
    private String determineContentType(String fileName) {
        if (fileName == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "xml" -> "application/xml";
            case "json" -> "application/json";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

    /**
     * 查询文件详情。
     */
    @GetMapping("/{id}")
    public Result<FileVO> getById(@PathVariable Long id) {
        FileVO vo = fileService.getById(id);
        return Result.success(vo);
    }

    /**
     * 删除文件。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success();
    }
}