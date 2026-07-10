package com.hrms.system.organization.dto;

/**
 * 字典数据查询条件 DTO。
 */
public class DictDataQueryDTO {

    private String typeCode;
    private String dictLabel;
    private Integer status;

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getDictLabel() {
        return dictLabel;
    }

    public void setDictLabel(String dictLabel) {
        this.dictLabel = dictLabel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}