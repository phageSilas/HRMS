package com.hrms.business.personnel.common.constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntryApplicationConstant {
    //ConcurrentHHashMap锁
    public static final Map<Long, Object> ENTRY_CONFIRM_LOCKS = new ConcurrentHashMap<>();
    //默认页号
    public static final int DEFAULT_PAGE_NUM = 1;

    //默认页大小
    public static final int DEFAULT_PAGE_SIZE = 20;

    //最大页大小
    public static final int MAX_PAGE_SIZE = 200;

}
