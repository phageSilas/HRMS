package com.hrms.system.auth.vo;

import java.util.List;

/**
 * 字段权限 VO。
 */
public class FieldPermissionVO {

    /**
     * 可见字段列表。
     */
    private List<String> viewableFields;

    /**
     * 可编辑字段列表。
     */
    private List<String> editableFields;

    /**
     * 流程必填字段列表。
     */
    private List<String> flowRequiredFields;

    public List<String> getViewableFields() {
        return viewableFields;
    }

    public void setViewableFields(List<String> viewableFields) {
        this.viewableFields = viewableFields;
    }

    public List<String> getEditableFields() {
        return editableFields;
    }

    public void setEditableFields(List<String> editableFields) {
        this.editableFields = editableFields;
    }

    public List<String> getFlowRequiredFields() {
        return flowRequiredFields;
    }

    public void setFlowRequiredFields(List<String> flowRequiredFields) {
        this.flowRequiredFields = flowRequiredFields;
    }
}