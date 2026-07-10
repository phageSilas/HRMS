package com.hrms.system.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置。
 */
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    private String basePath = "/uploads";
    private Long maxSize = 10 * 1024 * 1024L;
    private String allowedTypes = "jpg,jpeg,png,pdf,doc,docx,xls,xlsx";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public Long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }

    public String getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(String allowedTypes) {
        this.allowedTypes = allowedTypes;
    }
}