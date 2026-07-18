/**
 * AI 智能助手 API 服务
 */

import request from '@/utils/request';
import type { PageResult } from '@/types/api';

// ============ 类型定义 ============

export interface ChatRequest {
  conversationId?: number;
  content: string;
}

export interface Conversation {
  id: number;
  title: string;
  messageCount: number;
  lastMessage?: string;
  status: number;
  createTime: string;
  updateTime: string;
}

export interface Message {
  id: number;
  role: 'user' | 'assistant';
  content: string;
  metadata?: string;
  createTime: string;
}

export interface ConversationDetail {
  conversationId: number;
  title: string;
  messages: Message[];
}

// ============ SSE 事件类型 ============

export interface SseStartEvent {
  type: 'start';
  conversationId: number;
}

export interface SseContentEvent {
  type: 'content';
  text: string;
}

export interface SseEndEvent {
  type: 'end';
  reason: string;
}

export interface SseErrorEvent {
  type: 'error';
  code: number;
  message: string;
}

export type SseEvent = SseStartEvent | SseContentEvent | SseEndEvent | SseErrorEvent;

export interface SseCallbacks {
  onStart?: (conversationId: number) => void;
  onContent?: (text: string) => void;
  onEnd?: (reason: string, rawData?: string) => void;
  onError?: (code: number, message: string) => void;
}

// ============ API 方法 ============

/**
 * 发送消息（SSE 流式）
 *
 * 由于后端使用 POST + SSE，不能用原生 EventSource，
 * 这里用 fetch 手动读取响应流并解析 SSE 事件。
 *
 * @param abortSignal 可选 AbortSignal，用于中止请求
 * @returns 包含 abort() 方法的对象，可手动中止
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

/** 获取会话列表（分页） */
export async function getConversations(params?: { pageNum?: number; pageSize?: number }) {
  return request.get<PageResult<Conversation>>('/api/v1/ai/conversations', { params });
}

/** 获取消息记录 */
export async function getMessages(conversationId: number) {
  return request.get<ConversationDetail>(`/api/v1/ai/conversations/${conversationId}/messages`);
}

/** 删除会话 */
export async function deleteConversation(id: number) {
  return request.delete(`/api/v1/ai/conversations/${id}`);
}

/** 修改会话标题 */
export async function updateTitle(id: number, title: string): Promise<void> {
  return request.put(`/api/v1/ai/conversations/${id}/title`, { title });
}
