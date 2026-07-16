package com.hrms.business.salary.mq;

/**
 * 薪资模块 MQ 常量。
 */
public final class SalaryMqConstants {

    private SalaryMqConstants() {
    }

    public static final String SALARY_EXCHANGE = "hrms.salary.exchange";

    public static final String BATCH_CALCULATE_ROUTING_KEY = "salary.batch.calculate";

    public static final String BATCH_CALCULATE_QUEUE = "hrms.salary.batch.calculate.queue";
}
