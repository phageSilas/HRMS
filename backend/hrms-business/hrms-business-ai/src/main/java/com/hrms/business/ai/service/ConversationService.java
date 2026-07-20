package com.hrms.business.ai.service;

import com.hrms.business.ai.vo.ConversationDetailVO;
import com.hrms.business.ai.vo.ConversationVO;
import com.hrms.common.web.PageResult;

/**
 * 会话管理服务接口
 * <p>
 * 提供 AI 对话会话的 CRUD 操作，包括：列表分页查询、详情查询（含消息列表）、
 * 创建、更新标题、逻辑删除、消息计数递增。
 * </p>
 */
public interface ConversationService {

    /**
     * 分页查询当前用户的会话列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页会话列表
     */
    PageResult<ConversationVO> listConversations(Long userId, int pageNum, int pageSize);

    /**
     * 查询会话详情（含消息列表）
     *
     * @param userId         用户ID（用于权限校验）
     * @param conversationId 会话ID
     * @return 会话详情
     */
    ConversationDetailVO getConversationDetail(Long userId, Long conversationId);

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @param title  会话标题
     * @return 会话ID
     */
    Long createConversation(Long userId, String title);

    /**
     * 更新会话标题
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     * @param title          新标题
     */
    void updateTitle(Long userId, Long conversationId, String title);

    /**
     * 删除会话（逻辑删除）
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     */
    void deleteConversation(Long userId, Long conversationId);

    /**
     * 增加会话消息计数
     *
     * @param conversationId 会话ID
     */
    void incrementMessageCount(Long conversationId);

}
