package com.hrms.system.organization.dto;

/**
 * 职位创建请求 DTO。
 */
public class PostCreateDTO {

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