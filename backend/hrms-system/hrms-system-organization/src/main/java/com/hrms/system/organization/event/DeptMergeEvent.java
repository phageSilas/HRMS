package com.hrms.system.organization.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 部门合并事件
 * <p>
 * 当部门合并时发布此事件，用于触发员工迁移等后续操作
 * </p>
 */
@Getter
public class DeptMergeEvent extends ApplicationEvent {

    /**
     * 源部门ID（将被删除）
     */
    private final Long sourceDeptId;

    /**
     * 目标部门ID（接收员工）
     */
    private final Long targetDeptId;

    public DeptMergeEvent(Object source, Long sourceDeptId, Long targetDeptId) {
        super(source);
        this.sourceDeptId = sourceDeptId;
        this.targetDeptId = targetDeptId;
    }

}
