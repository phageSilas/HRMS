package com.hrms.system.auth.vo;

import java.util.List;

/**
 * 数据权限范围 VO。
 */
public class DataScopeVO {

    /**
     * 数据权限范围类型：1-本人 2-本部门 3-本部门及子部门 4-全部。
     */
    private Integer scopeType;

    /**
     * 部门 ID 列表（当 scopeType 为 2 或 3 时有值）。
     */
    private List<Long> departmentIds;

    public Integer getScopeType() {
        return scopeType;
    }

    public void setScopeType(Integer scopeType) {
        this.scopeType = scopeType;
    }

    public List<Long> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Long> departmentIds) {
        this.departmentIds = departmentIds;
    }
}