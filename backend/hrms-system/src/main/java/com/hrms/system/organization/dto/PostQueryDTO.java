package com.hrms.system.organization.dto;

/**
 * 职位查询条件 DTO。
 */
public class PostQueryDTO {

    /**
     * 职位名称（模糊查询）。
     */
    private String postName;

    /**
     * 职位编码（模糊查询）。
     */
    private String postCode;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}