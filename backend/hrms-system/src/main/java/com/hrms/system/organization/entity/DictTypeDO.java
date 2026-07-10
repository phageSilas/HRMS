package com.hrms.system.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 字典类型实体。
 */
@TableName("sys_dict_type")
public class DictTypeDO extends BaseEntity {

    /**
     * 字典类型编码。
     */
    private String typeCode;

    /**
     * 字典类型名称。
     */
    private String typeName;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}