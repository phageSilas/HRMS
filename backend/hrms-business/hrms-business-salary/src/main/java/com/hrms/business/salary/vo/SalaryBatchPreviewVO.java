package com.hrms.business.salary.vo;

import lombok.Data;

import java.util.List;

/**
 * 薪资批次预览返回视图。
 */
@Data
public class SalaryBatchPreviewVO {

    private SalaryBatchVO batch;

    private List<SalaryBatchItemVO> items;
}
