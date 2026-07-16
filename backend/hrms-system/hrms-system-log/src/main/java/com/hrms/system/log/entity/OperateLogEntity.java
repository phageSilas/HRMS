package com.hrms.system.log.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志实体（与 sys_operate_log 表对应）
 * 注意：sys_operate_log 表是日志表，不包含 BaseEntity 的全部公共字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_operate_log")
public class OperateLogEntity extends BaseEntity {

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 操作模块
     */
    private String operateModule;

    /**
     * 操作描述
     */
    private String operateDesc;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseResult;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 执行时长（毫秒）
     */
    private Integer executeTime;

    /**
     * 执行状态：1-成功 0-失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    // 重写 BaseEntity 中的字段，标记为数据库中不存在
    @TableField(exist = false)
    private Long createBy;

    @TableField(exist = false)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private Long updateBy;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer isDeleted;

    @TableField(exist = false)
    private Integer version;

}
