import HeaderActions from './HeaderActions';
import HeaderContent from './HeaderContent';
import UserMenu from './UserMenu';
import WorkTabs from './WorkTabs';
import type { UserInfo } from '@/types/user';
import { Avatar } from 'antd';
import React from 'react';
import styles from './index.less';

interface LayoutFrameProps {
  currentUser?: UserInfo;
  avatarUrl?: string;
  displayName: string;
  onLogout: () => void;
  children?: React.ReactNode;
}

const LayoutFrame: React.FC<LayoutFrameProps> = ({
  currentUser,
  avatarUrl,
  displayName,
  onLogout,
  children,
}) => {
  return (
    <div className={styles.layoutFrame}>
      <div className={styles.shellHeader}>
        <HeaderContent />
        <div className={styles.shellHeaderRight}>
          <HeaderActions />
          <UserMenu currentUser={currentUser} avatarUrl={avatarUrl} onLogout={onLogout}>
            <Avatar size={34} src={avatarUrl}>
              {displayName.slice(0, 1)}
            </Avatar>
          </UserMenu>
        </div>
      </div>
      <WorkTabs />
      <div className={styles.pageFrame}>{children}</div>
    </div>
  );
};

export default LayoutFrame;
