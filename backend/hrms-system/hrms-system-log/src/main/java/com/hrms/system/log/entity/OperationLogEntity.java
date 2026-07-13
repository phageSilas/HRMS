package com.hrms.system.log.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

/**
 * 操作日志实体
 */
@Data
public class OperationLogEntity extends BaseEntity {

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求路径
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
     * 执行时长（毫秒）
     */
    private Long executeTime;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 状态：1-成功，0-失败
     */
    private Integer status;

}
