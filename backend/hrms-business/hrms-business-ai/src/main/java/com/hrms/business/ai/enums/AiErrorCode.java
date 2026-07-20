package com.hrms.business.ai.enums;

import com.hrms.common.exception.ErrorCode;

/**
 * AI 智能助手模块错误码
 * <p>
 * 错误码范围：40120-40129
 * </p>
 *
 * @since 2026-07-20
 */
public class AiErrorCode {

    private AiErrorCode() {
    }

    /**
     * 40121 - AI 服务调用失败（DashScope 超时/不可用）
     */
    public static final ErrorCode AI_SERVICE_ERROR = new ErrorCode(40121, "AI 服务调用失败，请稍后重试");

    /**
     * 40123 - 会话不存在或不属于当前用户
     */
    public static final ErrorCode CONVERSATION_NOT_FOUND = new ErrorCode(40123, "会话不存在或无权访问");

}
