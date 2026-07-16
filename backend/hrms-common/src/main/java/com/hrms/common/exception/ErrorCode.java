package com.hrms.common.exception;

import lombok.Getter;

import java.io.Serializable;

/**
 * 错误码定义
 */
@Getter
public class ErrorCode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 成功码
     */
    public static final ErrorCode SUCCESS = new ErrorCode(20000, "操作成功");

    /**
     * 参数错误系列 (40000-40099)
     */
    public static final ErrorCode PARAM_VALIDATION_FAILED = new ErrorCode(40001, "参数校验失败");
    public static final ErrorCode PARAM_TYPE_MISMATCH = new ErrorCode(40002, "参数类型不匹配");
    public static final ErrorCode PARAM_REQUIRED = new ErrorCode(40003, "参数不能为空");
    public static final ErrorCode PARAM_FORMAT_ERROR = new ErrorCode(40004, "参数格式错误");

    /**
     * 请求方法不支持 (40050-40099)
     */
    public static final ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(40050, "不支持的请求方法");

    /**
     * 认证错误系列 (40100-40199)
     */
    public static final ErrorCode UNAUTHORIZED = new ErrorCode(40100, "未登录");
    public static final ErrorCode TOKEN_EXPIRED = new ErrorCode(40101, "Token已过期");
    public static final ErrorCode TOKEN_INVALID = new ErrorCode(40102, "Token无效");
    public static final ErrorCode ACCOUNT_LOCKED = new ErrorCode(40110, "账号已锁定");
    public static final ErrorCode ACCOUNT_DISABLED = new ErrorCode(40111, "账号已禁用");
    public static final ErrorCode ACCOUNT_EXPIRED = new ErrorCode(40112, "账号已过期");

    /**
     * 权限错误系列 (40300-40399)
     */
    public static final ErrorCode FORBIDDEN = new ErrorCode(40300, "无权限");
    public static final ErrorCode DATA_SCOPE_DENIED = new ErrorCode(40301, "数据权限不足");
    public static final ErrorCode MENU_PERMISSION_DENIED = new ErrorCode(40302, "菜单权限不足");

    /**
     * 资源错误系列 (40400-40499)
     */
    public static final ErrorCode NOT_FOUND = new ErrorCode(40400, "资源不存在");
    public static final ErrorCode RESOURCE_DELETED = new ErrorCode(40401, "资源已被删除");

    /**
     * 冲突错误系列 (40900-40999)
     */
    public static final ErrorCode CONFLICT = new ErrorCode(40900, "数据冲突");
    public static final ErrorCode DATA_DUPLICATE = new ErrorCode(40901, "数据重复");
    public static final ErrorCode VERSION_CONFLICT = new ErrorCode(40902, "版本冲突");

    /**
     * 系统错误系列 (50000-50099)
     */
    public static final ErrorCode SYSTEM_ERROR = new ErrorCode(50000, "系统内部错误");
    public static final ErrorCode DATABASE_ERROR = new ErrorCode(50001, "数据库操作失败");
    public static final ErrorCode REDIS_ERROR = new ErrorCode(50002, "Redis操作失败");
    public static final ErrorCode RPC_ERROR = new ErrorCode(50003, "远程调用失败");
    public static final ErrorCode FILE_UPLOAD_ERROR = new ErrorCode(50004, "文件上传失败");
    public static final ErrorCode FILE_DOWNLOAD_ERROR = new ErrorCode(50005, "文件下载失败");

    /**
     * 组织架构错误系列 (40020-40029)
     */
    public static final ErrorCode DEPT_CODE_EXISTS = new ErrorCode(40021, "部门编码已存在");
    public static final ErrorCode DEPT_PARENT_NOT_FOUND = new ErrorCode(40022, "上级部门不存在");
    public static final ErrorCode DEPT_LEVEL_EXCEED = new ErrorCode(40023, "部门层级超过最大限制（5级）");
    public static final ErrorCode DEPT_HAS_CHILDREN = new ErrorCode(40024, "存在子部门，无法删除");
    public static final ErrorCode DEPT_HAS_EMPLOYEES = new ErrorCode(40025, "存在在职员工，无法删除");

    /**
     * 字典错误系列 (40030-40039)
     */
    public static final ErrorCode DICT_TYPE_NOT_FOUND = new ErrorCode(40030, "字典类型编码不存在");

    /**
     * 员工模块错误系列 (40031-40039)
     */
    public static final ErrorCode EMPLOYEE_PHONE_EXISTS = new ErrorCode(40031, "手机号已被占用");
    public static final ErrorCode EMPLOYEE_DEPT_NOT_FOUND = new ErrorCode(40032, "所属部门不存在");
    public static final ErrorCode EMPLOYEE_POST_NOT_FOUND = new ErrorCode(40033, "职位不存在");
    public static final ErrorCode EMPLOYEE_NOT_FOUND = new ErrorCode(40034, "员工不存在");
    public static final ErrorCode EMPLOYEE_CANNOT_DELETE = new ErrorCode(40035, "该员工已转正，无法删除");
    public static final ErrorCode EMPLOYEE_HAS_BUSINESS = new ErrorCode(40036, "该员工存在业务记录，无法删除");

    public static final ErrorCode BUSINESS_ERROR = new ErrorCode(50100, "业务处理失败");

}
