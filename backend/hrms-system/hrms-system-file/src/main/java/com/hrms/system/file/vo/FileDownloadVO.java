package com.hrms.system.file.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件下载信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadVO {

    /**
     * 文件 ID。
     */
    private Long fileId;

    /**
     * 原始文件名。
     */
    private String fileName;

    /**
     * 存储路径。
     */
    private String filePath;

    /**
     * MIME 类型。
     */
    private String mimeType;
}
