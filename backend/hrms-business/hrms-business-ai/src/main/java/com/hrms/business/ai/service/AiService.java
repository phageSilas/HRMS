package com.hrms.business.ai.service;

/**
 * AI智能助手服务接口
 */
public interface AiService {

    /**
     * AI问答
     *
     * @param question 问题
     * @return 回答
     */
    String chat(String question);

    /**
     * 智能推荐
     *
     * @param userId 用户ID
     * @param type   推荐类型
     * @return 推荐结果
     */
    Object recommend(Long userId, String type);

    /**
     * 智能分析
     *
     * @param data 数据
     * @return 分析结果
     */
    Object analyze(Object data);

}
