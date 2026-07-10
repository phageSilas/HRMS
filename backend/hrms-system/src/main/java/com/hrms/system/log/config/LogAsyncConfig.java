package com.hrms.system.log.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 日志异步任务配置。
 *
 * <p>配置专门用于日志记录的线程池，避免阻塞主线程。</p>
 */
@Configuration
@EnableAsync
public class LogAsyncConfig {

    /**
     * 日志任务执行器。
     *
     * @return 线程池执行器
     */
    @Bean("logTaskExecutor")
    public Executor logTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(2);

        // 最大线程数
        executor.setMaxPoolSize(5);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程名称前缀
        executor.setThreadNamePrefix("log-async-");

        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler((r, e) -> {
            // 队列满时，直接在当前线程执行
            r.run();
        });

        executor.initialize();
        return executor;
    }
}