package com.hrms.business.salary.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 薪资账套分页返回视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTemplatePageVO {

    private Long id;

    private String templateName;

    private String templateCode;

    private String scopeType;

    private String scopeValue;

    private Integer status;

    private Integer itemCount;

    private String remark;

    private LocalDateTime createTime;

    private List<SalaryTemplateItemVO> items;
}
