import ModuleLanding from '@/components/ModuleLanding';
import { RobotOutlined } from '@ant-design/icons';
import React from 'react';

/**
 * AI 智能助手基础页面
 */
const AiPage: React.FC = () => (
  <ModuleLanding
    title="AI 智能助手"
    description="提供制度问答、流程引导、路由推荐和知识库管理能力，后续接入 SSE 对话与 RAG 检索。"
    icon={<RobotOutlined />}
    metrics={[
      { label: '推荐问题', value: 8, suffix: '个' },
      { label: '知识库文档', value: 0, suffix: '份' },
      { label: '可跳转流程', value: 9, suffix: '类' },
    ]}
    actions={[
      { label: '制度问答', color: 'blue' },
      { label: '操作引导', color: 'cyan' },
      { label: '路由推荐', color: 'green' },
      { label: '知识库管理', color: 'purple' },
    ]}
  />
);

export default AiPage;
