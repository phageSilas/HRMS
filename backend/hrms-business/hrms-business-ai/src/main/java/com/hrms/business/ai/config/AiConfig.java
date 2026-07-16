package com.hrms.business.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 智能助手模块配置
 * <p>
 * 从 application.yml 读取 hrms.ai.dashscope.* 配置
 * <p>
 * DashScope（阿里云百炼）统一负责：
 * - LLM 对话生成（通义千问系列模型）
 * - 知识库 RAG 检索（百炼知识库）
 * <p>
 * 使用百炼 OpenAI 兼容模式接口，无需额外 SDK。
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
     * API 基础地址（OpenAI 兼容模式）
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode";

    /**
     * 模型名称
     * <p>
     * 常见模型：
     * - qwen-plus（推荐，平衡性能与成本）
     * - qwen3.7-plus（最新，推荐）
     * - qwen-max（最强，价格更高）
     * - qwen-turbo（最快，适合简单场景）
     */
    private String model = "qwen3.7-plus";

    /**
     * 超时时间（毫秒）
     */
    private long timeout = 60000;

    /**
     * 知识库 ID（在百炼控制台创建知识库后获取）
     * 未配置时不影响基础对话，AI 仅凭自身知识回答。
     */
    private String knowledgeBaseId;

}
