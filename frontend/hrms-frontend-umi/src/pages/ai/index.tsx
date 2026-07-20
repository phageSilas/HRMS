/**
 * AI 智能助手对话页
 *
 * 布局：左侧会话列表 + 右侧对话区
 * 功能：SSE 流式对话、Markdown 渲染、会话切换、新建/删除/重命名会话、路由建议卡片
 *
 * 数据流：
 * - 会话列表通过手动调用 getConversations 管理（避免 useRequest 不可控的副作用）
 * - 消息加载通过 useRequest 封装 getMessages
 * - 发送消息通过 sendChatMessage（基于 fetch + ReadableStream 的 SSE 客户端）
 * - SSE 事件通过回调更新 messages 状态和 streamingContent
 * - 首页传入的待发送消息通过 sessionStorage 传递
 *
 * @module AiChat
 */

import {
  deleteConversation,
  getConversations,
  getMessages,
  sendChatMessage,
  updateTitle,
  type Conversation,
  type ConversationDetail,
  type Message,
} from '@/services/ai';
import type { PageResult } from '@/types/api';
import {
  DeleteOutlined,
  PauseCircleOutlined,
  PlusOutlined,
  RobotOutlined,
  SendOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import {
  Avatar,
  Button,
  Card,
  Input,
  List,
  Popconfirm,
  Spin,
  Tag,
  Typography,
  message as antMsg,
} from 'antd';
import React, { useCallback, useEffect, useRef, useState } from 'react';
import ReactMarkdown from 'react-markdown';
import rehypeRaw from 'rehype-raw';
import remarkGfm from 'remark-gfm';

const { Text, Title } = Typography;
const { TextArea } = Input;
/** sessionStorage 中首页待发送消息的 key */
const HOME_AI_PENDING_PROMPT_STORAGE_KEY = 'hrms-ai-pending-prompt';

// ============ 欢迎语 + 推荐问题 ============

/** 首次进入无会话时的欢迎文案 */
const WELCOME_MESSAGE =
  '你好！我是 HRMS 智能助手，可以帮你解答制度问题、查询流程、提供操作引导。';

/** 首页推荐问题列表（点击直接填入输入框） */
const SUGGESTED_QUESTIONS = [
  '今年的年假政策是什么？',
  '怎么申请加班？',
  '入职需要办理哪些手续？',
  '考勤打卡规则是什么？',
];

// ============ 样式常量 ============

/** 左侧会话栏宽度 */
const SIDEBAR_WIDTH = 280;
/** 聊天区头部高度 */
const HEADER_HEIGHT = 56;

const styles = {
  container: {
    display: 'flex',
    height: 'calc(100vh - 64px)',
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
    padding: '12px 16px',
    borderRadius: 12,
    lineHeight: 1.6,
    wordBreak: 'break-word',
    overflow: 'hidden',
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

// ============ Markdown 渲染覆盖样式 ============

/**
 * react-markdown 渲染 AI 回复的 Markdown 样式
 * 覆盖标题、代码块、表格、引用等元素的默认样式
 */
const markdownStyles = `
  .ai-markdown h1, .ai-markdown h2, .ai-markdown h3,
  .ai-markdown h4, .ai-markdown h5, .ai-markdown h6 {
    margin-top: 12px;
    margin-bottom: 8px;
    font-weight: 600;
  }
  .ai-markdown p { margin-bottom: 8px; }
  .ai-markdown ul, .ai-markdown ol { margin-bottom: 8px; padding-left: 20px; }
  .ai-markdown li { margin-bottom: 4px; }
  .ai-markdown code {
    background: #f0f0f0;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 0.9em;
  }
  .ai-markdown pre {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
    margin-bottom: 8px;
  }
  .ai-markdown pre code {
    background: none;
    padding: 0;
  }
  .ai-markdown table {
    border-collapse: collapse;
    margin-bottom: 8px;
    width: 100%;
  }
  .ai-markdown th, .ai-markdown td {
    border: 1px solid #e8e8e8;
    padding: 6px 10px;
    text-align: left;
  }
  .ai-markdown th {
    background: #fafafa;
    font-weight: 600;
  }
  .ai-markdown blockquote {
    border-left: 3px solid #1677ff;
    padding-left: 12px;
    margin: 8px 0;
    color: #666;
  }
  .ai-markdown a { color: #1677ff; }
`;

// ============ 建议卡片组件 ============

/** 路由建议：label 为按钮文本，path 为跳转路径 */
interface Suggestion {
  label: string;
  path: string;
}

/** 建议卡片组件：AI 返回的路由建议，以可点击卡片方式展示 */
interface SuggestionCardsProps {
  suggestions: Suggestion[];
}

const SuggestionCards: React.FC<SuggestionCardsProps> = ({ suggestions }) => {
  if (!suggestions || suggestions.length === 0) return null;

  return (
    <div style={{ marginTop: 12, marginBottom: 4 }}>
      <Text
        type="secondary"
        style={{ fontSize: 12, marginBottom: 6, display: 'block' }}
      >
        推荐操作：
      </Text>
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        {suggestions.map((s, i) => (
          <Card
            key={i}
            size="small"
            hoverable
            onClick={() => {
              history.push(s.path);
            }}
            style={{
              borderRadius: 8,
              border: '1px solid #e8e8e8',
              cursor: 'pointer',
              minWidth: 100,
            }}
            bodyStyle={{
              padding: '6px 12px',
              display: 'flex',
              alignItems: 'center',
              gap: 6,
            }}
          >
            <Tag color="blue" style={{ marginRight: 0, fontSize: 12 }}>
              {s.label}
            </Tag>
          </Card>
        ))}
      </div>
    </div>
  );
};

// ============ 消息气泡组件 ============

/** 消息气泡组件：用户消息（纯文本右对齐蓝色气泡） / AI 消息（Markdown 渲染左对齐灰色气泡） */
interface MessageBubbleProps {
  message: Message;
  isStreaming?: boolean;
}

const MessageBubble: React.FC<MessageBubbleProps> = ({
  message,
  isStreaming,
}) => {
  const isUser = message.role === 'user';

  // 解析 metadata 中的路由建议（AI 回复可附带可点击的路由建议卡片）
  let suggestions: Suggestion[] = [];
  if (!isUser && message.metadata) {
    try {
      const meta = JSON.parse(message.metadata);
      if (meta.suggestions) {
        suggestions = meta.suggestions;
      }
    } catch {
      // ignore parse error
    }
  }

  return (
    <div
      style={{
        ...styles.messageRow,
        flexDirection: isUser ? 'row-reverse' : 'row',
      }}
    >
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
          maxWidth: '70%',
          display: 'flex',
          flexDirection: 'column',
          alignItems: isUser ? 'flex-end' : 'flex-start',
        }}
      >
        {/* 消息气泡本体 */}
        <div
          style={{
            ...styles.messageBubble,
            width: 'fit-content',
            maxWidth: '100%',
            backgroundColor: isUser ? '#1677ff' : '#f5f5f5',
            color: isUser ? '#fff' : '#333',
            borderTopLeftRadius: isUser ? 12 : 4,
            borderTopRightRadius: isUser ? 4 : 12,
            whiteSpace: isUser ? 'pre-wrap' : 'normal',
            overflowWrap: 'break-word',
          }}
        >
          {isUser ? (
            message.content
          ) : (
            <div className="ai-markdown">
              <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                rehypePlugins={[rehypeRaw]}
                components={{
                  a: ({ href, children }) => {
                    if (href?.startsWith('/')) {
                      return (
                        <a
                          href={href}
                          onClick={(e) => {
                            e.preventDefault();
                            history.push(href);
                          }}
                        >
                          {children}
                        </a>
                      );
                    }
                    return (
                      <a
                        href={href}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        {children}
                      </a>
                    );
                  },
                }}
              >
                {message.content}
              </ReactMarkdown>
            </div>
          )}
          {isStreaming && <span style={styles.streamingCursor} />}
        </div>

        {/* 路由建议卡片 */}
        {!isUser && suggestions.length > 0 && (
          <SuggestionCards suggestions={suggestions} />
        )}
      </div>
    </div>
  );
};

// ============ 可编辑标题组件 ============

/** 可编辑标题组件：双击进入编辑模式，失去焦点或按 Enter 保存 */
interface EditableTitleProps {
  value: string;
  onSave: (newTitle: string) => void;
}

const EditableTitle: React.FC<EditableTitleProps> = ({ value, onSave }) => {
  const [editing, setEditing] = useState(false);
  const [inputValue, setInputValue] = useState(value);
  // 使用 React.ElementRef 获取 antd Input 组件正确的 ref 类型（InputRef）
  const inputRef = useRef<React.ElementRef<typeof Input>>(null);

  useEffect(() => {
    if (editing && inputRef.current) {
      inputRef.current.focus();
      inputRef.current.select();
    }
  }, [editing]);

  useEffect(() => {
    setInputValue(value);
  }, [value]);

  /** 保存标题（去掉首尾空格） */
  const handleSave = () => {
    const trimmed = inputValue.trim();
    if (trimmed && trimmed !== value) {
      onSave(trimmed);
    }
    setEditing(false);
  };

  if (editing) {
    return (
      <Input
        ref={inputRef}
        size="small"
        value={inputValue}
        onChange={(e) => setInputValue(e.target.value)}
        onBlur={handleSave}
        onPressEnter={handleSave}
        style={{ width: 200 }}
      />
    );
  }

  return (
    <Text
      style={{ cursor: 'pointer' }}
      ellipsis={{ tooltip: value }}
      onDoubleClick={() => setEditing(true)}
    >
      {value}
    </Text>
  );
};

// ============ SSE 事件解析扩展 ============

/** SSE end 事件扩展：携带路由建议（由后端在 end 事件中额外返回） */
interface SseEndEventExt {
  type: 'end';
  reason: string;
  suggestions?: Suggestion[];
}

/** 首页传入的待发送消息结构 */
interface PendingHomePromptPayload {
  content: string;
  createdAt: string;
  source?: 'home';
}

// ============ 主页面组件 ============

/** AI 智能助手主页面 */
const AiChatPage: React.FC = () => {
  // ---- 状态 ----
  // conversations 由 useMemo 派生自 convData（见下方 fetchConversations）
  const [currentId, setCurrentId] = useState<number | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamingContent, setStreamingContent] = useState('');
  const [error, setError] = useState<string | null>(null);
  /** 会话列表刷新标识 — 递增时重新加载 */
  const [listRefreshKey, setListRefreshKey] = useState(0);
  /** 首页传入的待发送 prompt */
  const [pendingHomePrompt, setPendingHomePrompt] = useState<string | null>(
    null,
  );

  const messageListRef = useRef<HTMLDivElement>(null);
  /** SSE 已收到的完整流式内容（streamingContent 的副本，用于在回调中获取最新值） */
  const streamingRef = useRef('');
  /** sendChatMessage 返回的 abort 函数 */
  const abortRef = useRef<(() => void) | null>(null);
  /**
   * SSE onStart 中收到的会话 ID
   * 用于区分：currentId 变化是由 SSE 触发的还是用户手动切换的
   */
  const sseConversationIdRef = useRef<number | null>(null);
  /** 首页待发送消息是否已消费 */
  const pendingHomePromptConsumedRef = useRef(false);
  /** 是否正在创建新会话（用于抑制自动选中第一条的逻辑） */
  const creatingNewConversationRef = useRef(false);

  // ---- 会话列表数据管理（手动管理，不用 useRequest 避免不可控） ----

  const [convData, setConvData] = useState<PageResult<Conversation> | null>(
    null,
  );
  const [convLoading, setConvLoading] = useState(false);
  const [convError, setConvError] = useState<any>(null);

  /** 加载会话列表 */
  const fetchConversations = useCallback(async () => {
    setConvLoading(true);
    try {
      const result = await getConversations({ pageNum: 1, pageSize: 50 });
      setConvData(result);
      setConvError(null);
    } catch (err: any) {
      setConvError(err);
    } finally {
      setConvLoading(false);
    }
  }, []);

  // 初始加载
  useEffect(() => {
    fetchConversations();
  }, [fetchConversations]);

  /**
   * 读取首页传入的待发送消息
   *
   * 从 sessionStorage 中读取首页用户输入的 prompt，
   * 读出后立即移除，避免重复消费。
   */
  useEffect(() => {
    const rawValue = sessionStorage.getItem(HOME_AI_PENDING_PROMPT_STORAGE_KEY);
    if (!rawValue) {
      return;
    }

    sessionStorage.removeItem(HOME_AI_PENDING_PROMPT_STORAGE_KEY);

    try {
      const parsed = JSON.parse(rawValue) as PendingHomePromptPayload;
      const content = parsed.content?.trim();
      if (content) {
        setPendingHomePrompt(content);
      }
    } catch {
      console.warn('[AI] 首页待发送消息解析失败');
    }
  }, []);

  // listRefreshKey 变化时重新加载
  useEffect(() => {
    if (listRefreshKey > 0) fetchConversations();
  }, [listRefreshKey, fetchConversations]);

  // 用 useMemo 避免不必要的重渲染
  const conversations = React.useMemo(
    () => convData?.records || [],
    [convData],
  );

  /** 加载消息记录 */
  const [msgLoading, setMsgLoading] = useState(false);
  const loadMessages = useCallback(async (id: number) => {
    setMsgLoading(true);
    try {
      const data = await getMessages(id);
      const detail = data as ConversationDetail;
      setMessages(detail.messages || []);
      setError(null);
    } catch {
      setError('加载消息失败');
    } finally {
      setMsgLoading(false);
    }
  }, []);

  /**
   * currentId 变化时加载对应会话的消息
   *
   * 注意：
   * 1. 如果 currentId 的变化是由 SSE onStart 触发的（新会话），
   *    跳过 loadMessages，因为 SSE 流式回调会自动填充消息，不需要从后端拉历史。
   * 2. 不能用布尔标志 isSseActiveRef 来判断 SSE 是否活跃，因为 SSE 事件可能
   *    在同一轮同步解析中完成（onStart→onContent→onEnd），标志会被提前重置，
   *    导致守卫失效。改用 sseConversationIdRef 比对 ID。
   */
  useEffect(() => {
    if (currentId && sseConversationIdRef.current === currentId) {
      sseConversationIdRef.current = null;
      return;
    }
    if (!currentId && creatingNewConversationRef.current) {
      return;
    }
    if (currentId) {
      loadMessages(currentId);
      setStreamingContent('');
    } else {
      setMessages([]);
    }
  }, [currentId]);

  /** 滚动消息列表到底部（延时 50ms 等待 DOM 更新） */
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

  // ---- 暂停当前回答 ----

  /**
   * 暂停 AI 回复
   *
   * 调用 abortRef 中止 SSE 请求，然后将已收到的流式内容
   * 作为完整助手消息添加到消息列表。
   */
  const handlePause = useCallback(() => {
    if (abortRef.current) {
      abortRef.current();
      abortRef.current = null;
    }
    // 把已收到但未呈现的流式内容作为最终消息保存
    const partialContent = streamingRef.current;
    if (partialContent) {
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now(),
          role: 'assistant',
          content: partialContent + '\n\n*（回答已中止）*',
          createTime: new Date().toISOString(),
        },
      ]);
    }
    setStreamingContent('');
    streamingRef.current = '';
    setIsStreaming(false);
  }, []);

  // ---- SSE 发送消息 ----

  /**
   * 发送消息（SSE 流式）
   *
   * 1. 根据参数决定是否强制新建会话
   * 2. 在 messages 中添加临时用户消息
   * 3. 调用 sendChatMessage 启动 SSE 连接
   * 4. 通过回调逐步更新 streamingContent 和 messages
   * 5. 结束时刷新会话列表
   *
   * @param rawContent  消息内容（去除首尾空格）
   * @param options.forceNewConversation 是否强制新建会话（首页进入时使用）
   */
  const sendMessageByContent = useCallback(
    async (
      rawContent: string,
      options?: { forceNewConversation?: boolean },
    ) => {
      const content = rawContent.trim();
      if (!content || isStreaming) return;

      const shouldCreateNewConversation =
        options?.forceNewConversation ?? false;

      if (shouldCreateNewConversation) {
        creatingNewConversationRef.current = true;
        setCurrentId(null);
        setMessages([]);
        setStreamingContent('');
        setError(null);
      }

      setInputValue('');
      setIsStreaming(true);
      setError(null);
      streamingRef.current = '';
      abortRef.current = null;
      sseConversationIdRef.current = null;

      // 添加临时用户消息
      const tempUserMsg: Message = {
        id: Date.now(),
        role: 'user',
        content,
        createTime: new Date().toISOString(),
      };

      setMessages((prev) => [...prev, tempUserMsg]);

      let pendingSuggestions: Suggestion[] = [];
      const targetConversationId = shouldCreateNewConversation
        ? undefined
        : currentId || undefined;

      const { abort } = sendChatMessage(
        { conversationId: targetConversationId, content },
        {
          onStart: (conversationId) => {
            // 记录 SSE 分配的会话 ID，用于在 currentId 变化时
            // 避免重复加载历史消息（见 currentId useEffect 守卫）
            sseConversationIdRef.current = conversationId;
            creatingNewConversationRef.current = false;
            if (conversationId !== currentId) {
              setCurrentId(conversationId);
            }
          },
          onContent: (text) => {
            // 累加增量内容到 streamingRef 和 state
            streamingRef.current += text;
            setStreamingContent(streamingRef.current);
          },
          onEnd: (reason, rawData) => {
            abortRef.current = null;
            // 如果用户手动中止，已由 handlePause 处理
            if (reason === 'abort') return;

            // 解析 end 事件中的路由建议
            if (rawData) {
              try {
                const parsed: SseEndEventExt = JSON.parse(rawData);
                if (parsed.suggestions) {
                  pendingSuggestions = parsed.suggestions;
                }
              } catch {
                // ignore
              }
            }

            const finalContent = streamingRef.current;
            if (finalContent) {
              setMessages((prev) => [
                ...prev,
                {
                  id: Date.now(),
                  role: 'assistant',
                  content: finalContent,
                  metadata:
                    pendingSuggestions.length > 0
                      ? JSON.stringify({ suggestions: pendingSuggestions })
                      : undefined,
                  createTime: new Date().toISOString(),
                },
              ]);
            }
            setStreamingContent('');
            streamingRef.current = '';
            setIsStreaming(false);
            // 刷新会话列表使最新消息出现在侧边栏
            setListRefreshKey((k) => k + 1);
          },
          onError: (code, msg) => {
            abortRef.current = null;
            creatingNewConversationRef.current = false;
            // 即使出错，已有消息已在数据库，刷新列表让新会话出现在侧边栏
            setListRefreshKey((k) => k + 1);
            antMsg.error(msg || 'AI 响应异常');
            setError(msg || 'AI 响应异常，请稍后重试');
            setStreamingContent('');
            streamingRef.current = '';
            setIsStreaming(false);
          },
        },
      );
      abortRef.current = abort;
    },
    [inputValue, isStreaming, currentId, setListRefreshKey],
  );

  /** 点击发送按钮触发 */
  const handleSend = useCallback(async () => {
    await sendMessageByContent(inputValue);
  }, [inputValue, sendMessageByContent]);

  /**
   * 消费首页传入的待发送消息
   *
   * 在 isStreaming 为 false 时发送（避免与用户当前操作冲突）。
   */
  useEffect(() => {
    if (
      !pendingHomePrompt ||
      pendingHomePromptConsumedRef.current ||
      isStreaming
    ) {
      return;
    }

    pendingHomePromptConsumedRef.current = true;
    void sendMessageByContent(pendingHomePrompt, {
      forceNewConversation: true,
    });
    setPendingHomePrompt(null);
  }, [isStreaming, pendingHomePrompt, sendMessageByContent]);

  // ---- 键盘事件 ----

  /** Enter 发送消息（Shift+Enter 换行） */
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  // ---- 新建会话 ----

  const handleNewConversation = () => {
    creatingNewConversationRef.current = true;
    setCurrentId(null);
    setMessages([]);
    setStreamingContent('');
    setError(null);
    setInputValue('');
  };

  // ---- 删除会话 ----

  /**
   * 删除会话
   *
   * 如果删除的是当前会话，自动切换到最近会话。
   */
  const handleDeleteConversation = async (id: number) => {
    try {
      await deleteConversation(id);
      antMsg.success('已删除');
      if (id === currentId) {
        const remaining = conversations.filter((c) => c.id !== id);
        setCurrentId(remaining.length > 0 ? remaining[0].id : null);
      }
      setListRefreshKey((k) => k + 1);
    } catch {
      antMsg.error('删除失败');
    }
  };

  // ---- 重命名会话 ----

  const handleRenameConversation = async (id: number, title: string) => {
    try {
      await updateTitle(id, title);
      antMsg.success('标题已更新');
      setListRefreshKey((k) => k + 1);
    } catch {
      antMsg.error('修改标题失败');
    }
  };

  // ---- 渲染 ----

  return (
    <AiErrorBoundary>
      <div style={styles.container}>
        <style>{`
        @keyframes blink {
          0%, 100% { opacity: 1; }
          50% { opacity: 0; }
        }
        ${markdownStyles}
      `}</style>

        {/* ===== 左侧：会话列表 ===== */}
        <div style={styles.sidebar}>
          <div style={styles.sidebarHeader}>
            <Text strong style={{ fontSize: 16 }}>
              历史对话
            </Text>
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
                <Button
                  size="small"
                  onClick={fetchConversations}
                  style={{ marginTop: 8 }}
                >
                  重试
                </Button>
              </div>
            ) : conversations.length === 0 ? (
              <div style={{ textAlign: 'center', padding: 40 }}>
                <Text type="secondary">暂无历史对话</Text>
                <br />
                <Text type="secondary" style={{ fontSize: 12 }}>
                  点击「新建」开始对话
                </Text>
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
                      backgroundColor:
                        currentId === item.id ? '#e6f4ff' : 'transparent',
                      borderLeft:
                        currentId === item.id
                          ? '3px solid #1677ff'
                          : '3px solid transparent',
                      transition: 'all 0.2s',
                    }}
                    actions={[
                      <Popconfirm
                        key="delete"
                        title="确认删除此对话？"
                        onConfirm={() => handleDeleteConversation(item.id)}
                      >
                        <DeleteOutlined
                          style={{ color: '#999', fontSize: 12 }}
                        />
                      </Popconfirm>,
                    ]}
                  >
                    <List.Item.Meta
                      title={
                        currentId === item.id ? (
                          <EditableTitle
                            value={item.title}
                            onSave={(newTitle) =>
                              handleRenameConversation(item.id, newTitle)
                            }
                          />
                        ) : (
                          <Text
                            style={{ fontSize: 14 }}
                            ellipsis={{ tooltip: item.title }}
                          >
                            {item.title}
                          </Text>
                        )
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
          <div style={styles.chatHeader}>
            <Title level={5} style={{ margin: 0 }}>
              AI 智能助手
            </Title>
          </div>

          <div style={styles.messageList} ref={messageListRef}>
            {msgLoading ? (
              <div style={{ textAlign: 'center', padding: 60 }}>
                <Spin tip="加载消息中..." />
              </div>
            ) : error ? (
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
            ) : messages.length === 0 ? (
              <div style={styles.welcomeContainer}>
                <RobotOutlined
                  style={{ fontSize: 64, color: '#1677ff', marginBottom: 24 }}
                />
                <Title level={4} style={{ margin: 0 }}>
                  AI 智能助手
                </Title>
                <Text type="secondary" style={{ marginTop: 12, maxWidth: 480 }}>
                  {WELCOME_MESSAGE}
                </Text>
                <div
                  style={{
                    marginTop: 32,
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: 12,
                    justifyContent: 'center',
                  }}
                >
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
            ) : (
              <>
                {messages.map((msg) => (
                  <MessageBubble key={msg.id} message={msg} />
                ))}

                {/* 流式内容气泡：正在接收的 AI 回复 */}
                {isStreaming && streamingContent && (
                  <MessageBubble
                    message={{
                      id: 0,
                      role: 'assistant',
                      content: streamingContent,
                      createTime: '',
                    }}
                    isStreaming
                  />
                )}

                {/* 思考中占位：SSE 已连接但尚未收到内容块 */}
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

                {/* 错误提示 + 重试（仅在已有消息时显示） */}
                {error && messages.length > 0 && (
                  <div style={{ textAlign: 'center', padding: 12 }}>
                    <Text type="danger" style={{ fontSize: 12 }}>
                      {error}
                    </Text>
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
            {isStreaming ? (
              <Button
                danger
                icon={<PauseCircleOutlined />}
                onClick={handlePause}
                style={{ height: 52 }}
              >
                暂停
              </Button>
            ) : (
              <Button
                type="primary"
                icon={<SendOutlined />}
                onClick={handleSend}
                disabled={!inputValue.trim()}
                style={{ height: 52 }}
              >
                发送
              </Button>
            )}
          </div>
        </div>
      </div>
    </AiErrorBoundary>
  );
};

export default AiChatPage;

// ============ 错误边界 ============

/**
 * AI 页面渲染错误边界
 *
 * 捕获子组件渲染时的异常，展示降级 UI 并允许用户刷新重试，
 * 避免整个页面白屏。
 */
class AiErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean; error: any }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, error: null };
  }
  static getDerivedStateFromError(error: any) {
    return { hasError: true, error };
  }
  componentDidCatch(error: any, info: any) {
    console.error('[AiChat Error]', error, info);
  }
  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: 40, textAlign: 'center' }}>
          <div style={{ color: '#ff4d4f', fontSize: 24, marginBottom: 12 }}>
            ⚠️
          </div>
          <div style={{ color: '#ff4d4f', marginBottom: 8 }}>页面渲染异常</div>
          <div style={{ color: '#999', fontSize: 12 }}>
            {this.state.error?.message || '未知错误'}
          </div>
          <button
            onClick={() => window.location.reload()}
            style={{ marginTop: 16, padding: '8px 24px', cursor: 'pointer' }}
          >
            刷新重试
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
