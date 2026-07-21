/**
 * AI 智能助手 API 服务
 *
 * 提供 AI 对话相关接口，包括 SSE 流式对话、会话管理（CRUD）、消息历史查询。
 *
 * @module Services.AI
 */

import request from '@/utils/request';
import type { PageResult } from '@/types/api';

// ============ 类型定义 ============

/** 聊天请求参数 */
export interface ChatRequest {
  /** 会话 ID，新建会话时不传 */
  conversationId?: number;
  /** 用户输入的消息内容 */
  content: string;
}

/** 会话概要 */
export interface Conversation {
  /** 会话 ID */
  id: number;
  /** 会话标题 */
  title: string;
  /** 消息数量 */
  messageCount: number;
  /** 最后一条消息摘要 */
  lastMessage?: string;
  /** 会话状态（0=正常） */
  status: number;
  /** 创建时间 */
  createTime: string;
  /** 更新时间 */
  updateTime: string;
}

/** 单条消息 */
export interface Message {
  /** 消息 ID */
  id: number;
  /** 发送角色：用户/助手 */
  role: 'user' | 'assistant';
  /** 消息内容（文本或 Markdown） */
  content: string;
  /** 元数据（JSON 字符串，预留扩展） */
  metadata?: string;
  /** 发送时间 */
  createTime: string;
}

/** 会话详情（含消息列表） */
export interface ConversationDetail {
  /** 会话 ID */
  conversationId: number;
  /** 会话标题 */
  title: string;
  /** 消息记录列表 */
  messages: Message[];
}

// ============ SSE 事件类型 ============

/** SSE 流开始事件：携带后端分配的会话 ID */
export interface SseStartEvent {
  type: 'start';
  /** 后端分配的会话 ID（新建会话时返回） */
  conversationId: number;
}

/** SSE 内容块事件：携带一段文本增量 */
export interface SseContentEvent {
  type: 'content';
  /** 本次增量文本片段 */
  text: string;
}

/** SSE 流结束事件：携带结束原因 */
export interface SseEndEvent {
  type: 'end';
  /**
   * 结束原因
   * - "stop": 正常结束
   * - "abort": 用户中止
   * - "length": 超出长度限制截断
   */
  reason: string;
}

/** SSE 错误事件：携带错误码和描述 */
export interface SseErrorEvent {
  type: 'error';
  /** 错误码 */
  code: number;
  /** 错误描述 */
  message: string;
}

/** SSE 事件联合类型 */
export type SseEvent = SseStartEvent | SseContentEvent | SseEndEvent | SseErrorEvent;

/** SSE 回调函数集合 */
export interface SseCallbacks {
  /** 流开始回调 */
  onStart?: (conversationId: number) => void;
  /** 内容块回调（可多次触发） */
  onContent?: (text: string) => void;
  /** 流结束回调 */
  onEnd?: (reason: string, rawData?: string) => void;
  /** 错误回调 */
  onError?: (code: number, message: string) => void;
}

// ============ API 方法 ============

/**
 * 发送消息（SSE 流式）
 *
 * 由于后端使用 POST + SSE，不能用原生 EventSource，
 * 这里用 fetch 手动读取响应流并解析 SSE 事件。
 *
 * @param request     聊天请求参数（含会话 ID 和消息内容）
 * @param callbacks   SSE 事件回调集合
 * @param abortSignal 可选 AbortSignal，用于从外部中止请求
 * @returns 包含 abort() 方法的对象，可手动中止请求
 */
export function sendChatMessage(
  request: ChatRequest,
  callbacks: SseCallbacks,
  abortSignal?: AbortSignal,
): { abort: () => void } {
  const abortController = new AbortController();
  const signal = abortSignal || abortController.signal;

  const token = localStorage.getItem('token');

  (async () => {
    try {
      const response = await fetch('/api/v1/ai/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify(request),
        signal,
      });

      if (!response.ok) {
        callbacks.onError?.(response.status, '网络请求失败');
        return;
      }

      const reader = response.body?.getReader();
      if (!reader) {
        callbacks.onError?.(500, '无法读取响应流');
        return;
      }

      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        buffer += chunk;

        // 按行解析 SSE
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          // SSE 格式: data:xxx 或 data: xxx，处理两者
          if (line.startsWith('data:')) {
            const jsonStr = line.slice(5).trim();
            if (!jsonStr) continue;
            try {
              const event: SseEvent = JSON.parse(jsonStr);
              handleSseEvent(event, callbacks, jsonStr);
            } catch (e) {
              console.warn('SSE 数据解析失败:', jsonStr);
            }
          }
        }
      }
    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.log('[SSE] 请求已中止');
        callbacks.onEnd?.('abort');
      } else {
        console.error('[SSE] 读取异常:', err);
        callbacks.onError?.(500, '响应流读取异常');
      }
    }
    console.log('[SSE] 流读取结束');
  })();

  return { abort: () => abortController.abort() };
}

/**
 * SSE 事件分发
 *
 * 根据事件类型调用对应的回调函数，rawJson 传入 onEnd 用于
 * 前端额外解析（如获取路由建议卡片数据）。
 *
 * @param event    解析后的 SSE 事件对象
 * @param callbacks 回调集合
 * @param rawJson  原始 JSON 字符串（仅 end 事件使用）
 */
function handleSseEvent(event: SseEvent, callbacks: SseCallbacks, rawJson?: string): void {
  switch (event.type) {
    case 'start':
      callbacks.onStart?.(event.conversationId);
      break;
    case 'content':
      callbacks.onContent?.(event.text);
      break;
    case 'end':
      callbacks.onEnd?.(event.reason, rawJson);
      break;
    case 'error':
      callbacks.onError?.(event.code, event.message);
      break;
  }
}

/**
 * 获取会话列表（分页）
 *
 * @param params.pageNum 页码（从 1 开始）
 * @param params.pageSize 每页条数
 * @returns 分页的会话列表
 */
export async function getConversations(params?: { pageNum?: number; pageSize?: number }) {
  return request.get<PageResult<Conversation>>('/api/v1/ai/conversations', { params });
}

/**
 * 获取指定会话的消息记录
 *
 * @param conversationId 会话 ID
 * @returns 会话详情（含消息列表）
 */
export async function getMessages(conversationId: number) {
  return request.get<ConversationDetail>(`/api/v1/ai/conversations/${conversationId}/messages`);
}

/**
 * 删除会话
 *
 * @param id 会话 ID
 */
export async function deleteConversation(id: number) {
  return request.delete(`/api/v1/ai/conversations/${id}`);
}

/**
 * 修改会话标题
 *
 * @param id    会话 ID
 * @param title 新标题
 */
export async function updateTitle(id: number, title: string): Promise<void> {
  return request.put(`/api/v1/ai/conversations/${id}/title`, { title });
}
