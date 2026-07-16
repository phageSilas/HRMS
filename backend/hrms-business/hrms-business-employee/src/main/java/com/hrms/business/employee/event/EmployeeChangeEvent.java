package com.hrms.business.employee.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 员工变更事件
 * <p>
 * 员工增删改时发布此事件，用于更新部门人数缓存
 * </p>
 */
@Getter
public class EmployeeChangeEvent extends ApplicationEvent {

    /**
     * 变更类型：CREATE-创建, DELETE-删除, UPDATE-更新（调部门）
     */
    private final ChangeType changeType;

    /**
     * 员工ID
     */
    private final Long employeeId;

    /**
     * 原部门ID（UPDATE时有效）
     */
    private final Long oldDeptId;

    /**
     * 新部门ID（UPDATE时有效）
     */
    private final Long newDeptId;

    public EmployeeChangeEvent(Object source, ChangeType changeType, Long employeeId) {
        super(source);
        this.changeType = changeType;
        this.employeeId = employeeId;
        this.oldDeptId = null;
        this.newDeptId = null;
    }

    public EmployeeChangeEvent(Object source, ChangeType changeType, Long employeeId, Long oldDeptId, Long newDeptId) {
        super(source);
        this.changeType = changeType;
        this.employeeId = employeeId;
        this.oldDeptId = oldDeptId;
        this.newDeptId = newDeptId;
    }

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        CREATE, DELETE, UPDATE
    }

}
