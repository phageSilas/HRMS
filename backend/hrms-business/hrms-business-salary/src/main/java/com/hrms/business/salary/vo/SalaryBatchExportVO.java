package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 薪资批次导出结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryBatchExportVO {

    /**
     * 文件 ID。
     */
    private Long fileId;

    /**
     * 文件名。
     */
    private String fileName;

    /**
     * 下载地址。
     */
    private String downloadUrl;
}
