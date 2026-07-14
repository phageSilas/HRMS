package com.hrms.business.salary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 创建或更新薪资账套请求参数。
 */
@Data
public class SalaryTemplateCreateOrUpdateRequestDTO {

    @NotBlank
    private String templateName;

    private String templateCode;

    private String scopeType;

    private String scopeValue;

    private Integer status;

    private String remark;

    @Valid
    private List<SalaryTemplateItemRequestDTO> items;
}
