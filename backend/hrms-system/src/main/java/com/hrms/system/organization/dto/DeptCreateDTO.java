package com.hrms.system.organization.dto;

/**
 * 部门创建请求 DTO。
 */
public class DeptCreateDTO {

    /**
     * 上级部门 ID。
     */
    private Long parentId;

    /**
     * 部门名称。
     */
    private String deptName;

    /**
     * 部门编码。
     */
    private String deptCode;

    /**
     * 部门负责人用户 ID。
     */
    private Long leaderUserId;

    /**
     * 部门负责人员工 ID。
     */
    private Long leaderEmployeeId;

    /**
     * 排序号。
     */
    private Integer sortNo;

    /**
     * 部门描述。
     */
    private String description;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public Long getLeaderUserId() {
        return leaderUserId;
    }

    public void setLeaderUserId(Long leaderUserId) {
        this.leaderUserId = leaderUserId;
    }

    public Long getLeaderEmployeeId() {
        return leaderEmployeeId;
    }

    public void setLeaderEmployeeId(Long leaderEmployeeId) {
        this.leaderEmployeeId = leaderEmployeeId;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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