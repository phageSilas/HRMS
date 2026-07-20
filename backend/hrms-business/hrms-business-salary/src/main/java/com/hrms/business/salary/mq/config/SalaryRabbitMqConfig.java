package com.hrms.business.salary.mq.config;

import com.hrms.business.salary.mq.constants.SalaryMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 薪资模块 RabbitMQ 交换机、队列和绑定配置。
 */
@Configuration
public class SalaryRabbitMqConfig {

    /**
     * 声明薪资模块交换机。
     *
     * @return 薪资模块交换机
     * 本方法使用的工具类: DirectExchange(spring-amqp)
     */
    @Bean
    public DirectExchange salaryExchange() {
        return new DirectExchange(SalaryMqConstants.SALARY_EXCHANGE, true, false);
    }

    /**
     * 声明薪资批次核算队列。
     *
     * @return 薪资批次核算队列
     * 本方法使用的工具类: Queue(spring-amqp)
     */
    @Bean
    public Queue salaryBatchCalculateQueue() {
        return new Queue(SalaryMqConstants.BATCH_CALCULATE_QUEUE, true);
    }

    /**
     * 绑定薪资批次核算队列。
     *
     * @param salaryExchange 薪资模块交换机
     * @param salaryBatchCalculateQueue 薪资批次核算队列
     * @return 队列绑定关系
     * 本方法使用的工具类: BindingBuilder(spring-amqp)
     */
    @Bean
    public Binding salaryBatchCalculateBinding(DirectExchange salaryExchange,
                                               @Qualifier("salaryBatchCalculateQueue")
                                               Queue salaryBatchCalculateQueue) {
        return BindingBuilder.bind(salaryBatchCalculateQueue)//指定要绑定的队列
                .to(salaryExchange)//指定要绑定的交换机
                .with(SalaryMqConstants.BATCH_CALCULATE_ROUTING_KEY);//指定绑定时使用的路由键
    }
}
