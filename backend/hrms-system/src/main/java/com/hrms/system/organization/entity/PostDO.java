package com.hrms.system.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 职位实体。
 *
 * <p>对应数据库表 sys_post，存储职位信息。</p>
 */
@TableName("sys_post")
public class PostDO extends BaseEntity {

    /**
     * 职位名称。
     */
    private String postName;

    /**
     * 职位编码。
     */
    private String postCode;

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

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
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