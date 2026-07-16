/**
 * AI 智能助手对话页
 *
 * 布局：左侧会话列表 + 右侧对话区
 * 功能：SSE 流式对话、会话切换、新建会话
 */

import {
  DeleteOutlined,
  PlusOutlined,
  RobotOutlined,
  SendOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useRequest } from '@umijs/max';
import {
  Avatar,
  Button,
  Input,
  List,
  message as antMsg,
  Popconfirm,
  Spin,
  Typography,
} from 'antd';
import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  type Conversation,
  type Message,
  getConversations,
  getMessages,
  sendChatMessage,
} from '@/services/ai';

const { Text, Title } = Typography;
const { TextArea } = Input;

// ============ 欢迎语 + 推荐问题 ============

const WELCOME_MESSAGE = '你好！我是 HRMS 智能助手，可以帮你解答制度问题、查询流程、提供操作引导。';

const SUGGESTED_QUESTIONS = [
  '今年的年假政策是什么？',
  '怎么申请加班？',
  '入职需要办理哪些手续？',
  '考勤打卡规则是什么？',
];

// ============ 样式常量 ============

const SIDEBAR_WIDTH = 280;
const HEADER_HEIGHT = 56;

const styles = {
  container: {
    display: 'flex',
    height: 'calc(100vh - 64px)', // 减去顶部导航
    background: '#f5f5f5',
  } as React.CSSProperties,
  sidebar: {
    width: SIDEBAR_WIDTH,
    background: '#fff',
    borderRight: '1px solid #e8e8e8',
    display: 'flex',
    flexDirection: 'column',
  } as React.CSSProperties,
  sidebarHeader: {
    padding: '12px 16px',
    borderBottom: '1px solid #f0f0f0',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  } as React.CSSProperties,
  sidebarList: {
    flex: 1,
    overflow: 'auto',
    padding: '8px 0',
  } as React.CSSProperties,
  chatArea: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    background: '#fff',
  } as React.CSSProperties,
  chatHeader: {
    height: HEADER_HEIGHT,
    borderBottom: '1px solid #f0f0f0',
    display: 'flex',
    alignItems: 'center',
    padding: '0 24px',
  } as React.CSSProperties,
  messageList: {
    flex: 1,
    overflow: 'auto',
    padding: '24px',
  } as React.CSSProperties,
  messageRow: {
    display: 'flex',
    marginBottom: 20,
    gap: 12,
  } as React.CSSProperties,
  messageBubble: {
    maxWidth: '70%',
    padding: '12px 16px',
    borderRadius: 12,
    lineHeight: 1.6,
    whiteSpace: 'pre-wrap',
    wordBreak: 'break-word',
  } as React.CSSProperties,
  inputArea: {
    borderTop: '1px solid #f0f0f0',
    padding: '16px 24px',
    display: 'flex',
    gap: 12,
    alignItems: 'flex-end',
  } as React.CSSProperties,
  welcomeContainer: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 48,
    textAlign: 'center',
  } as React.CSSProperties,
  streamingCursor: {
    display: 'inline-block',
    width: 8,
    height: 16,
    background: '#1677ff',
    marginLeft: 2,
    animation: 'blink 1s step-end infinite',
  } as React.CSSProperties,
};

// ============ 消息气泡组件 ============

interface MessageBubbleProps {
  message: Message;
  isStreaming?: boolean;
}

const MessageBubble: React.FC<MessageBubbleProps> = ({ message, isStreaming }) => {
  const isUser = message.role === 'user';

  return (
    <div style={{ ...styles.messageRow, flexDirection: isUser ? 'row-reverse' : 'row' }}>
      <Avatar
        size={36}
        icon={isUser ? <UserOutlined /> : <RobotOutlined />}
        style={{
          backgroundColor: isUser ? '#1677ff' : '#52c41a',
          flexShrink: 0,
        }}
      />
      <div
        style={{
          ...styles.messageBubble,
          backgroundColor: isUser ? '#1677ff' : '#f5f5f5',
          color: isUser ? '#fff' : '#333',
          borderTopLeftRadius: isUser ? 12 : 4,
          borderTopRightRadius: isUser ? 4 : 12,
        }}
      >
        {message.content}
        {isStreaming && <span style={styles.streamingCursor} />}
      </div>
    </div>
  );
};

// ============ 主页面组件 ============

const AiChatPage: React.FC = () => {
  // ---- 状态 ----
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [currentId, setCurrentId] = useState<number | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamingContent, setStreamingContent] = useState('');
  const [error, setError] = useState<string | null>(null);

  const messageListRef = useRef<HTMLDivElement>(null);
  const streamingRef = useRef(''); // 跟踪流式内容（避免闭包问题）

  // ---- 数据加载 ----

  // 会话列表
  const {
    loading: convLoading,
    error: convError,
    refresh: refreshConversations,
  } = useRequest(
    () => getConversations({ pageNum: 1, pageSize: 50 }),
    {
      onSuccess: (data) => {
        setConversations(data?.records || []);
        // 自动选择第一个会话
        if (data?.records?.length > 0 && !currentId) {
          setCurrentId(data.records[0].id);
        }
      },
      refreshDeps: [],
    },
  );

  // 消息列表
  const { loading: msgLoading, run: loadMessages } = useRequest(
    (id: number) => getMessages(id),
    {
      manual: true,
      onSuccess: (data) => {
        setMessages(data?.messages || []);
        setError(null);
      },
      onError: () => {
        setError('加载消息失败');
      },
    },
  );

  // 切换会话时加载消息
  useEffect(() => {
    if (currentId) {
      loadMessages(currentId);
      setStreamingContent('');
    } else {
      setMessages([]);
    }
  }, [currentId]);

  // 滚动到底部
  const scrollToBottom = useCallback(() => {
    setTimeout(() => {
      if (messageListRef.current) {
        messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
      }
    }, 50);
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, streamingContent, scrollToBottom]);

  // ---- SSE 发送消息 ----

  const handleSend = useCallback(async () => {
    const content = inputValue.trim();
    if (!content || isStreaming) return;

    setInputValue('');
    setIsStreaming(true);
    setError(null);
    streamingRef.current = '';

    // 立即显示用户消息
    const tempUserMsg: Message = {
      id: Date.now(),
      role: 'user',
      content,
      createTime: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, tempUserMsg]);

    await sendChatMessage(
      { conversationId: currentId || undefined, content },
      {
        onStart: (conversationId) => {
          if (conversationId !== currentId) {
            setCurrentId(conversationId);
          }
        },
        onContent: (text) => {
          streamingRef.current += text;
          setStreamingContent(streamingRef.current);
        },
        onEnd: () => {
          // 将 streaming 内容转为正式消息
          const finalContent = streamingRef.current;
          if (finalContent) {
            setMessages((prev) => [
              ...prev,
              {
                id: Date.now(),
                role: 'assistant' as const,
                content: finalContent,
                createTime: new Date().toISOString(),
              },
            ]);
          }
          setStreamingContent('');
          streamingRef.current = '';
          setIsStreaming(false);
          refreshConversations();
        },
        onError: (code, msg) => {
          antMsg.error(msg || 'AI 响应异常');
          setError(msg || 'AI 响应异常，请稍后重试');
          setStreamingContent('');
          streamingRef.current = '';
          setIsStreaming(false);
        },
      },
    );
  }, [inputValue, isStreaming, currentId, refreshConversations]);

  // ---- 键盘事件 ----

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  // ---- 新建会话 ----

  const handleNewConversation = () => {
    setCurrentId(null);
    setMessages([]);
    setStreamingContent('');
    setError(null);
  };

  // ---- 渲染 ----

  return (
    <div style={styles.container}>
      {/* 光标闪烁动画 */}
      <style>{`
        @keyframes blink {
          0%, 100% { opacity: 1; }
          50% { opacity: 0; }
        }
      `}</style>

      {/* ===== 左侧：会话列表 ===== */}
      <div style={styles.sidebar}>
        <div style={styles.sidebarHeader}>
          <Text strong style={{ fontSize: 16 }}>历史对话</Text>
          <Button
            type="primary"
            size="small"
            icon={<PlusOutlined />}
            onClick={handleNewConversation}
          >
            新建
          </Button>
        </div>

        <div style={styles.sidebarList}>
          {convLoading ? (
            <div style={{ textAlign: 'center', padding: 40 }}>
              <Spin />
            </div>
          ) : convError ? (
            <div style={{ textAlign: 'center', padding: 40 }}>
              <Text type="warning">加载失败</Text>
              <Button size="small" onClick={refreshConversations} style={{ marginTop: 8 }}>
                重试
              </Button>
            </div>
          ) : conversations.length === 0 ? (
            <div style={{ textAlign: 'center', padding: 40 }}>
              <Text type="secondary">暂无历史对话</Text>
              <br />
              <Text type="secondary" style={{ fontSize: 12 }}>点击「新建」开始对话</Text>
            </div>
          ) : (
            <List
              dataSource={conversations}
              renderItem={(item) => (
                <List.Item
                  key={item.id}
                  onClick={() => setCurrentId(item.id)}
                  style={{
                    cursor: 'pointer',
                    padding: '10px 16px',
                    backgroundColor: currentId === item.id ? '#e6f4ff' : 'transparent',
                    borderLeft: currentId === item.id ? '3px solid #1677ff' : '3px solid transparent',
                    transition: 'all 0.2s',
                  }}
                  actions={[
                    <Popconfirm
                      key="delete"
                      title="确认删除此对话？"
                      onConfirm={() => {
                        // TODO: 调用删除 API
                        antMsg.success('已删除');
                      }}
                    >
                      <DeleteOutlined style={{ color: '#999', fontSize: 12 }} />
                    </Popconfirm>,
                  ]}
                >
                  <List.Item.Meta
                    title={
                      <Text
                        style={{ fontSize: 14 }}
                        ellipsis={{ tooltip: item.title }}
                      >
                        {item.title}
                      </Text>
                    }
                    description={
                      <div>
                        <Text
                          type="secondary"
                          style={{ fontSize: 12 }}
                          ellipsis
                        >
                          {item.lastMessage || `${item.messageCount} 条消息`}
                        </Text>
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          )}
        </div>
      </div>

      {/* ===== 右侧：对话区 ===== */}
      <div style={styles.chatArea}>
        {/* 会话标题 */}
        <div style={styles.chatHeader}>
          <Title level={5} style={{ margin: 0 }}>
            {currentId
              ? (conversations.find((c) => c.id === currentId)?.title || '对话')
              : 'AI 智能助手'}
          </Title>
        </div>

        {/* 消息列表 */}
        <div style={styles.messageList} ref={messageListRef}>
          {msgLoading ? (
            <div style={{ textAlign: 'center', padding: 60 }}>
              <Spin tip="加载消息中..." />
            </div>
          ) : !currentId && messages.length === 0 ? (
            // 欢迎页
            <div style={styles.welcomeContainer}>
              <RobotOutlined style={{ fontSize: 64, color: '#1677ff', marginBottom: 24 }} />
              <Title level={4} style={{ margin: 0 }}>AI 智能助手</Title>
              <Text type="secondary" style={{ marginTop: 12, maxWidth: 480 }}>
                {WELCOME_MESSAGE}
              </Text>
              <div style={{ marginTop: 32, display: 'flex', flexWrap: 'wrap', gap: 12, justifyContent: 'center' }}>
                {SUGGESTED_QUESTIONS.map((q) => (
                  <Button
                    key={q}
                    type="default"
                    shape="round"
                    onClick={() => {
                      setInputValue(q);
                    }}
                  >
                    {q}
                  </Button>
                ))}
              </div>
            </div>
          ) : error && messages.length === 0 ? (
            // 错误状态
            <div style={{ textAlign: 'center', padding: 60 }}>
              <Text type="warning">{error}</Text>
              <br />
              <Button
                type="primary"
                style={{ marginTop: 16 }}
                onClick={() => currentId && loadMessages(currentId)}
              >
                重试
              </Button>
            </div>
          ) : (
            <>
              {/* 消息气泡 */}
              {messages.map((msg) => (
                <MessageBubble key={msg.id} message={msg} />
              ))}

              {/* 流式渲染中的 AI 消息 */}
              {isStreaming && streamingContent && (
                <MessageBubble
                  message={{ id: 0, role: 'assistant', content: streamingContent, createTime: '' }}
                  isStreaming
                />
              )}

              {/* 等待中… */}
              {isStreaming && !streamingContent && (
                <div style={{ ...styles.messageRow, flexDirection: 'row' }}>
                  <Avatar
                    size={36}
                    icon={<RobotOutlined />}
                    style={{ backgroundColor: '#52c41a', flexShrink: 0 }}
                  />
                  <div
                    style={{
                      ...styles.messageBubble,
                      backgroundColor: '#f5f5f5',
                      color: '#999',
                      borderTopLeftRadius: 4,
                    }}
                  >
                    <Spin size="small" /> 思考中...
                  </div>
                </div>
              )}

              {/* 错误提示 + 重试 */}
              {error && messages.length > 0 && (
                <div style={{ textAlign: 'center', padding: 12 }}>
                  <Text type="danger" style={{ fontSize: 12 }}>{error}</Text>
                  <Button
                    size="small"
                    type="link"
                    onClick={() => {
                      setError(null);
                      handleSend();
                    }}
                  >
                    重试
                  </Button>
                </div>
              )}
            </>
          )}
        </div>

        {/* 输入区 */}
        <div style={styles.inputArea}>
          <TextArea
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="输入你的问题，按 Enter 发送，Shift+Enter 换行"
            rows={2}
            maxLength={2000}
            disabled={isStreaming}
            style={{ flex: 1, resize: 'none' }}
          />
          <Button
            type="primary"
            icon={<SendOutlined />}
            onClick={handleSend}
            loading={isStreaming}
            disabled={!inputValue.trim() || isStreaming}
            style={{ height: 52 }}
          >
            发送
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AiChatPage;
