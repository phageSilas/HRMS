package com.hrms.business.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.ai.entity.ConversationEntity;
import com.hrms.business.ai.entity.MessageEntity;
import com.hrms.business.ai.enums.AiErrorCode;
import com.hrms.business.ai.mapper.ConversationMapper;
import com.hrms.business.ai.mapper.AiMessageMapper;
import com.hrms.business.ai.service.ConversationService;
import com.hrms.business.ai.vo.ConversationDetailVO;
import com.hrms.business.ai.vo.ConversationVO;
import com.hrms.business.ai.vo.MessageVO;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;

    @Override
    public PageResult<ConversationVO> listConversations(Long userId, int pageNum, int pageSize) {
        // 查询当前用户的活跃会话，按更新时间倒序
        Page<ConversationEntity> page = conversationMapper.selectPage(
                Page.of(pageNum, pageSize),
                Wrappers.<ConversationEntity>lambdaQuery()
                        .eq(ConversationEntity::getUserId, userId)
                        .orderByDesc(ConversationEntity::getUpdateTime)
        );

        // 转换 VO
        List<ConversationVO> list = page.getRecords().stream()
                .map(this::toConversationVO)
                .collect(Collectors.toList());

        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public ConversationDetailVO getConversationDetail(Long userId, Long conversationId) {
        // 校验会话归属
        ConversationEntity conversation = getAndCheckOwnership(userId, conversationId);

        // 查询消息列表
        List<MessageEntity> messages = messageMapper.selectList(
                Wrappers.<MessageEntity>lambdaQuery()
                        .eq(MessageEntity::getConversationId, conversationId)
                        .orderByAsc(MessageEntity::getCreateTime)
        );

        // 转换 VO
        List<MessageVO> messageVOs = messages.stream()
                .map(this::toMessageVO)
                .collect(Collectors.toList());

        ConversationDetailVO detail = new ConversationDetailVO();
        detail.setConversationId(conversation.getId());
        detail.setTitle(conversation.getTitle());
        detail.setMessages(messageVOs);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConversation(Long userId, String title) {
        ConversationEntity entity = new ConversationEntity();
        entity.setUserId(userId);
        entity.setTitle(title);
        entity.setStatus(1); // 活跃
        entity.setMessageCount(0);
        conversationMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTitle(Long userId, Long conversationId, String title) {
        ConversationEntity conversation = getAndCheckOwnership(userId, conversationId);
        conversation.setTitle(title);
        conversationMapper.updateById(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long userId, Long conversationId) {
        ConversationEntity conversation = getAndCheckOwnership(userId, conversationId);
        // 逻辑删除会话（BaseEntity 的 isDeleted 字段有 @TableLogic）
        conversationMapper.deleteById(conversation.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementMessageCount(Long conversationId) {
        ConversationEntity entity = conversationMapper.selectById(conversationId);
        if (entity != null) {
            entity.setMessageCount(entity.getMessageCount() + 1);
            conversationMapper.updateById(entity);
        }
    }

    // ========== 私有方法 ==========

    /**
     * 查询会话并校验所属用户
     */
    private ConversationEntity getAndCheckOwnership(Long userId, Long conversationId) {
        ConversationEntity conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !userId.equals(conversation.getUserId())) {
            throw new GlobalException(AiErrorCode.CONVERSATION_NOT_FOUND);
        }
        return conversation;
    }

    /**
     * ConversationEntity → ConversationVO
     */
    private ConversationVO toConversationVO(ConversationEntity entity) {
        ConversationVO vo = new ConversationVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setMessageCount(entity.getMessageCount());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        // 查询最后一条消息作为摘要
        MessageEntity lastMsg = messageMapper.selectOne(
                Wrappers.<MessageEntity>lambdaQuery()
                        .eq(MessageEntity::getConversationId, entity.getId())
                        .orderByDesc(MessageEntity::getCreateTime)
                        .last("LIMIT 1")
        );
        if (lastMsg != null) {
            String summary = lastMsg.getContent();
            if (summary != null && summary.length() > 50) {
                summary = summary.substring(0, 50) + "...";
            }
            vo.setLastMessage(summary);
        }
        return vo;
    }

    /**
     * MessageEntity → MessageVO
     */
    private MessageVO toMessageVO(MessageEntity entity) {
        MessageVO vo = new MessageVO();
        vo.setId(entity.getId());
        vo.setRole(entity.getRole());
        vo.setContent(entity.getContent());
        vo.setMetadata(entity.getMetadata());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

}
