package com.hrms.business.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AI 智能助手模块配置
 * <p>
 * 从 application.yml 读取 hrms.ai.dashscope.* 配置
 * <p>
 * DashScope（阿里云百炼）统一负责：
 * - LLM 对话生成（通义千问系列模型）
 * - 知识库 RAG 检索（百炼应用知识库）
 * <p>
 * 使用百炼 App Completion API，在百炼控制台创建「应用」并关联知识库后，
 * 通过应用 ID 调用一个接口即可完成检索 + 生成。
 */
@Data
@Component
@ConfigurationProperties(prefix = "hrms.ai.dashscope")
public class AiConfig {

    /**
     * DashScope API Key（阿里云百炼）
     * 环境变量：DASHSCOPE_API_KEY
     */
    private String apiKey;

    /**
     * 百炼应用 ID
     * 环境变量：DASHSCOPE_APP_ID
     * <p>
     * 在百炼控制台创建「应用」（类型选智能助手），关联已有知识库，
     * 发布后获取 App ID。应用内部已配置系统 Prompt 和知识库。
     * 未配置时回退到基础对话模式（无知识库）。
     */
    private String appId;

    /**
     * API 基础地址
     */
    private String baseUrl = "https://dashscope.aliyuncs.com";

    /**
     * 模型名称（未配置 AppId 回退到基础对话时使用）
     * <p>
     * 常见模型：
     * - qwen-plus（推荐，平衡性能与成本）
     * - qwen-max（最强，价格更高）
     * - qwen-turbo（最快，适合简单场景）
     */
    private String model = "qwen-plus";

    /**
     * 超时时间（毫秒）
     */
    private long timeout = 60000;

    /**
     * AI 对话专用线程池
     * <p>
     * 用于 AI 对话流式处理的异步任务，替代默认的 ForkJoinPool.commonPool()。
     * 核心线程数 = 2，最大线程数 = 4，避免大量对话请求耗尽 Tomcat 线程。
     * 空闲存活 60 秒，使用有界队列防止无限积压。
     * </p>
     *
     * @return AI 对话 Executor
     */
    @Bean("aiChatExecutor")
    public Executor aiChatExecutor() {
        return new ThreadPoolExecutor(
                2,                          // 核心线程数
                4,                          // 最大线程数
                60L,                        // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),  // 有界队列，最多积压 100 个任务
                new ThreadPoolExecutor.CallerRunsPolicy()  // 队列满时由调用线程执行
        );
    }

}
