package com.hrms.system.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 部门实体。
 *
 * <p>对应数据库表 sys_dept，存储部门树形结构信息。</p>
 */
@TableName("sys_dept")
public class DeptDO extends BaseEntity {

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
     * 祖级路径（如：0,1,2）。
     */
    private String ancestors;

    /**
     * 部门层级。
     */
    private Integer deptLevel;

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
     * 在职员工数缓存。
     */
    private Integer employeeCount;

    /**
     * 版本号。
     */
    private Integer version;

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

    public String getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
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

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}