package com.hrms.system.organization.vo;

import java.util.List;

/**
 * 部门树 VO。
 */
public class DeptTreeVO {

    /**
     * 部门 ID。
     */
    private Long id;

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
     * 部门层级。
     */
    private Integer deptLevel;

    /**
     * 排序号。
     */
    private Integer sortNo;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 子部门列表。
     */
    private List<DeptTreeVO> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getDeptLevel() {
        return deptLevel;
    }

    public void setDeptLevel(Integer deptLevel) {
        this.deptLevel = deptLevel;
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

    public List<DeptTreeVO> getChildren() {
        return children;
    }

    public void setChildren(List<DeptTreeVO> children) {
        this.children = children;
    }
}