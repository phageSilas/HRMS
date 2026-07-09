package com.hrms.system.vo;

import java.util.List;

/**
 * 部门树返回对象。
 *
 * @param id 部门ID
 * @param name 部门名称
 * @param parentId 上级部门ID
 * @param children 子部门列表
 */
public record DepartmentTreeVO(
    Long id,
    String name,
    Long parentId,
    List<DepartmentTreeVO> children
) {
}
