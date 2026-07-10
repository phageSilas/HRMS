package com.hrms.system.organization.dto;

/**
 * 字典类型查询条件 DTO。
 */
public class DictTypeQueryDTO {

    private String typeCode;
    private String typeName;
    private Integer status;

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}