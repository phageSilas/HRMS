package com.hrms.system.auth.exception;

import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;

/**
 * 认证异常
 */
public class AuthException extends GlobalException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
