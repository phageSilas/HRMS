package com.hrms.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 更新用户 DTO
 */
@Data
@Schema(description = "更新用户请求体")
public class UserUpdateDTO {

    @Schema(description = "真实姓名", example = "张三（已转正）")
    private String realName;

    @Schema(description = "手机号", example = "13912345679")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan_new@hrms.com")
    private String email;

    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Integer status;

    @Schema(description = "角色ID列表", example = "[2, 3, 4]")
    private List<Long> roleIds;

    @Schema(description = "部门ID", example = "1001")
    private Long deptId;

}
