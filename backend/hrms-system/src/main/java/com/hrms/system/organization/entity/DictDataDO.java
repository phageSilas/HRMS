package com.hrms.system.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 字典数据实体。
 */
@TableName("sys_dict_data")
public class DictDataDO extends BaseEntity {

    /**
     * 字典类型编码。
     */
    private String typeCode;

    /**
     * 字典标签。
     */
    private String dictLabel;

    /**
     * 字典值。
     */
    private String dictValue;

    /**
     * 排序号。
     */
    private Integer sortNo;

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

    public String getDictLabel() {
        return dictLabel;
    }

    public void setDictLabel(String dictLabel) {
        this.dictLabel = dictLabel;
    }

    public String getDictValue() {
        return dictValue;
    }

    public void setDictValue(String dictValue) {
        this.dictValue = dictValue;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
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