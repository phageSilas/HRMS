package com.hrms.business.personnel.common.constant;

import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.common.exception.ErrorCode;

import java.util.List;

public class RegularApplicationConstant {
    // 评价标签
    public static final String TAB_EVALUATED = "evaluated";

    // 就业状态-试用期
    public static final int EMPLOYMENT_STATUS_PROBATION = 1;

    // 默认页码
    public static final int DEFAULT_PAGE_NUM = 1;

    // 默认页大
    public static final int DEFAULT_PAGE_SIZE = 20;

    // 最大页大小
    public static final int MAX_PAGE_SIZE = 200;

    // 待审批状态
    public static final List<Integer> PENDING_APPROVAL_STATUSES = List.of(
            ApplicationStatusEnum.DRAFT.getCode(),
            ApplicationStatusEnum.APPROVING.getCode()
    );

    // 已评价状态
    public static final List<Integer> EVALUATED_APPROVAL_STATUSES = List.of(
            ApplicationStatusEnum.APPROVED.getCode(),
            ApplicationStatusEnum.REJECTED.getCode(),
            ApplicationStatusEnum.ENTERED.getCode()
    );
}
