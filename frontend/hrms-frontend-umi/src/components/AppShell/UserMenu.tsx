import type { UserInfo } from '@/types/user';
import {
  BookOutlined,
  LockOutlined,
  LogoutOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import { Avatar, Button, Divider, Popover, Space, Typography, message } from 'antd';
import React from 'react';
import styles from './index.less';

const { Text } = Typography;

interface UserMenuProps {
  currentUser?: UserInfo;
  avatarUrl: string;
  children: React.ReactNode;
  onLogout: () => void;
}

const UserMenu: React.FC<UserMenuProps> = ({ currentUser, avatarUrl, children, onLogout }) => {
  const displayName =
    currentUser?.nickname || currentUser?.realName || currentUser?.username || '未登录';

  const content = (
    <div className={styles.userPanel}>
      <div className={styles.userSummary}>
        <Avatar size={44} src={avatarUrl}>
          {displayName.slice(0, 1)}
        </Avatar>
        <div>
          <strong>{displayName}</strong>
          <Text type="secondary">{currentUser?.username || 'internal user'}</Text>
          <Text type="secondary">
            {currentUser?.deptName || 'HRMS'} {currentUser?.roleName || currentUser?.roleCode || ''}
          </Text>
        </div>
      </div>
      <Divider />
      <Space direction="vertical" size={4} className={styles.userActions}>
        <Button type="text" icon={<UserOutlined />} onClick={() => history.push('/profile/index')}>
          个人中心
        </Button>
        <Button
          type="text"
          icon={<SafetyCertificateOutlined />}
          onClick={() => history.push('/profile/security')}
        >
          账号安全
        </Button>
        <Button type="text" icon={<BookOutlined />} onClick={() => history.push('/home')}>
          使用文档
        </Button>
        <Button type="text" icon={<LockOutlined />} onClick={() => message.info('锁屏功能待接入')}>
          锁定屏幕
        </Button>
      </Space>
      <Divider />
      <Button block icon={<LogoutOutlined />} onClick={onLogout}>
        退出登录
      </Button>
    </div>
  );

  return (
    <Popover
      placement="bottomRight"
      content={content}
      trigger="click"
      arrow={false}
      overlayClassName={styles.userPopover}
    >
      <span className={styles.avatarTrigger}>{children}</span>
    </Popover>
  );
};

export default UserMenu;
