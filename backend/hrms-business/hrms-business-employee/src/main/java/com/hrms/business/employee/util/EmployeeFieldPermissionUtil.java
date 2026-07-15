package com.hrms.business.employee.util;

import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字段权限校验工具类
 * <p>
 * 用于校验员工档案字段的可见性和可编辑性
 * </p>
 */
@Slf4j
public class EmployeeFieldPermissionUtil {

    private EmployeeFieldPermissionUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 默认可见字段（所有用户可见）
     */
    private static final Set<String> DEFAULT_VIEWABLE_FIELDS = Set.of(
            "id", "employeeNo", "employeeName", "gender", "phone", "email",
            "deptId", "deptName", "postId", "postName", "jobLevel",
            "leaderId", "leaderName", "workLocation", "hireType",
            "employmentStatus", "hireDate", "createTime"
    );

    /**
     * 默认可编辑字段（普通员工可编辑自己的字段）
     */
    private static final Set<String> DEFAULT_EDITABLE_FIELDS = Set.of(
            "email", "currentAddress", "emergencyContact", "emergencyPhone"
    );

    /**
     * 需走流程字段（变更需审批）
     */
    private static final Set<String> DEFAULT_FLOW_REQUIRED_FIELDS = Set.of(
            "phone", "deptId", "postId", "jobLevel", "baseSalary"
    );

    /**
     * 锁定字段（仅 HR/管理员可见）
     */
    private static final Set<String> DEFAULT_LOCKED_FIELDS = Set.of(
            "idCardNo", "bankAccount"
    );

    /**
     * 获取字段权限配置
     * <p>
     * 根据角色返回字段权限配置
     * </p>
     *
     * @param isAdmin 是否为管理员/HR
     * @return 字段权限配置
     */
    public static Map<String, List<String>> getFieldPermissions(boolean isAdmin) {
        if (isAdmin) {
            // 管理员/HR：所有字段可见可编辑
            return Map.of(
                    "editableFields", getAllFields(),
                    "viewableFields", getAllFields(),
                    "flowRequiredFields", List.of(),
                    "lockedFields", List.of()
            );
        }

        // 普通员工：受限权限
        return Map.of(
                "editableFields", List.copyOf(DEFAULT_EDITABLE_FIELDS),
                "viewableFields", List.copyOf(DEFAULT_VIEWABLE_FIELDS),
                "flowRequiredFields", List.copyOf(DEFAULT_FLOW_REQUIRED_FIELDS),
                "lockedFields", List.copyOf(DEFAULT_LOCKED_FIELDS)
        );
    }

    /**
     * 过滤不可见字段
     * <p>
     * 根据字段权限过滤掉不可见的字段
     * </p>
     *
     * @param fieldPermissions 字段权限配置
     * @param fieldName        字段名
     * @return 是否可见
     */
    public static boolean isViewable(Map<String, List<String>> fieldPermissions, String fieldName) {
        if (fieldPermissions == null) {
            return true;
        }
        List<String> viewableFields = fieldPermissions.get("viewableFields");
        if (viewableFields == null || viewableFields.isEmpty()) {
            return true;
        }
        return viewableFields.contains(fieldName);
    }

    /**
     * 判断是否可编辑
     *
     * @param fieldPermissions 字段权限配置
     * @param fieldName        字段名
     * @return 是否可编辑
     */
    public static boolean isEditable(Map<String, List<String>> fieldPermissions, String fieldName) {
        if (fieldPermissions == null) {
            return false;
        }
        List<String> editableFields = fieldPermissions.get("editableFields");
        if (editableFields == null || editableFields.isEmpty()) {
            return false;
        }
        return editableFields.contains(fieldName);
    }

    /**
     * 判断是否需走流程
     *
     * @param fieldPermissions 字段权限配置
     * @param fieldName        字段名
     * @return 是否需走流程
     */
    public static boolean isFlowRequired(Map<String, List<String>> fieldPermissions, String fieldName) {
        if (fieldPermissions == null) {
            return false;
        }
        List<String> flowRequiredFields = fieldPermissions.get("flowRequiredFields");
        if (flowRequiredFields == null || flowRequiredFields.isEmpty()) {
            return false;
        }
        return flowRequiredFields.contains(fieldName);
    }

    /**
     * 校验字段权限
     * <p>
     * 如果字段不可编辑，抛出异常
     * </p>
     *
     * @param fieldPermissions 字段权限配置
     * @param fieldName        字段名
     */
    public static void validateEditable(Map<String, List<String>> fieldPermissions, String fieldName) {
        if (!isEditable(fieldPermissions, fieldName)) {
            log.warn("字段 [{}] 无编辑权限", fieldName);
            throw new GlobalException(ErrorCode.FORBIDDEN, "字段 [" + fieldName + "] 无编辑权限");
        }
    }

    /**
     * 获取所有字段列表
     */
    private static List<String> getAllFields() {
        return Arrays.asList(
                "id", "employeeNo", "userId", "employeeName", "gender", "phone", "email",
                "idCardNo", "birthday", "domicileAddress", "currentAddress",
                "deptId", "deptName", "postId", "postName", "jobLevel",
                "leaderId", "leaderName", "workLocation", "hireType",
                "employmentStatus", "hireDate", "probationMonth", "probationSalaryRatio",
                "contractType", "contractExpireDate", "salaryTemplateId", "baseSalary",
                "bankAccount", "bankName", "emergencyContact", "emergencyPhone",
                "createTime", "remark"
        );
    }

}
