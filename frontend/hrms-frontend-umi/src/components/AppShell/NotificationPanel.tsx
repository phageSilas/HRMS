import {
  BellOutlined,
  CheckCircleOutlined,
  MailOutlined,
  NotificationOutlined,
  SoundOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import { Button, Empty, List, Tabs, Typography } from 'antd';
import React from 'react';
import styles from './index.less';

const { Text } = Typography;

interface NoticeItem {
  title: string;
  time: string;
  type: 'notice' | 'message' | 'mail' | 'todo';
}

const iconMap = {
  notice: <BellOutlined />,
  message: <SoundOutlined />,
  mail: <MailOutlined />,
  todo: <CheckCircleOutlined />,
};

const notices: NoticeItem[] = [
  { title: '入职申请待提交审批', time: '今天 09:20', type: 'notice' },
  { title: '转正评估将在 3 天后到期', time: '昨天 18:04', type: 'notice' },
  { title: '薪资批次核算完成', time: '2026-07-15 16:30', type: 'mail' },
  { title: '考勤异常统计已生成', time: '2026-07-15 09:10', type: 'message' },
];

const messages: NoticeItem[] = [
  { title: '审批中心有新的流程评论', time: '今天 10:16', type: 'message' },
  { title: '员工档案字段权限已更新', time: '昨天 15:42', type: 'mail' },
  { title: 'AI 助手知识库同步完成', time: '2026-07-14 20:15', type: 'notice' },
];

const todos: NoticeItem[] = [
  { title: '待处理入职确认', time: '今天 11:30', type: 'todo' },
  { title: '待复核离职交接', time: '明天 09:00', type: 'todo' },
];

function renderList(items: NoticeItem[]) {
  if (items.length === 0) {
    return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无内容" />;
  }

  return (
    <List
      dataSource={items}
      renderItem={(item) => (
        <List.Item className={styles.noticeItem}>
          <span className={`${styles.noticeIcon} ${styles[item.type]}`}>{iconMap[item.type]}</span>
          <span className={styles.noticeText}>
            <span>{item.title}</span>
            <Text type="secondary">{item.time}</Text>
          </span>
        </List.Item>
      )}
    />
  );
}

const NotificationPanel: React.FC = () => {
  return (
    <div className={styles.noticePanel}>
      <div className={styles.noticeHeader}>
        <strong>通知</strong>
        <Button type="text" size="small">
          标为已读
        </Button>
      </div>
      <Tabs
        size="small"
        items={[
          { key: 'notice', label: `通知 (${notices.length})`, children: renderList(notices) },
          { key: 'message', label: `消息 (${messages.length})`, children: renderList(messages) },
          { key: 'todo', label: `待办 (${todos.length})`, children: renderList(todos) },
        ]}
      />
      <Button block className={styles.noticeMore} onClick={() => history.push('/approval/workspace')}>
        查看全部
      </Button>
    </div>
  );
};

export default NotificationPanel;
