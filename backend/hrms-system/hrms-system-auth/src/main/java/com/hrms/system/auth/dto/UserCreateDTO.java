package com.hrms.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 创建用户 DTO
 */
@Data
@Schema(description = "创建用户请求体")
public class UserCreateDTO {

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "zhangsan")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "Zs@123456")
    private String password;

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String realName;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13912345678")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@hrms.com")
    private String email;

    @Schema(description = "角色ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[2, 3]")
    private List<Long> roleIds;

    @Schema(description = "关联员工ID", example = "null")
    private Long employeeId;

}
