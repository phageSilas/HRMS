package com.hrms.system.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件模块配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "hrms.file")
public class FileConfig {

    /**
     * 文件存储根目录。
     */
    private String baseDir = System.getProperty("user.dir") + "/data/export";
}
