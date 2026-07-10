package com.hrms.system.auth.dto;

/**
 * 用户更新请求 DTO。
 */
public class UserUpdateDTO {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 真实姓名。
     */
    private String realName;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 关联员工 ID。
     */
    private Long employeeId;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}