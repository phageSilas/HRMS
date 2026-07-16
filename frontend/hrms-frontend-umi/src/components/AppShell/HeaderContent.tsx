import { getRouteTrail, shellRoutes } from './routeMeta';
import {
  AppstoreOutlined,
  HomeOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { history, useLocation } from '@umijs/max';
import { Breadcrumb, Button, Popover, Space, Tooltip } from 'antd';
import React from 'react';
import styles from './index.less';

interface HeaderContentProps {
  defaultDom?: React.ReactNode;
}

const HeaderContent: React.FC<HeaderContentProps> = ({ defaultDom }) => {
  const location = useLocation();
  const trail = getRouteTrail(location.pathname);
  const quickRoutes = shellRoutes.filter((item) =>
    ['/home', '/process/entry', '/attendance/record', '/salary/batch', '/approval/workspace', '/ai'].includes(
      item.path
    )
  );

  return (
    <div className={styles.headerContent}>
      <div className={styles.headerDefaultDom}>{defaultDom}</div>
      <Tooltip title="刷新当前页面">
        <Button
          type="text"
          className={styles.iconButton}
          icon={<ReloadOutlined />}
          onClick={() => window.location.reload()}
        />
      </Tooltip>
      <Popover
        placement="bottomLeft"
        arrow={false}
        content={
          <div className={styles.quickPanel}>
            {quickRoutes.map((item) => (
              <Button key={item.path} type="text" onClick={() => history.push(item.path)}>
                <Space>
                  {item.icon || <HomeOutlined />}
                  {item.title}
                </Space>
              </Button>
            ))}
          </div>
        }
      >
        <Button type="text" className={styles.iconButton} icon={<AppstoreOutlined />} />
      </Popover>
      <Breadcrumb
        className={styles.breadcrumb}
        items={trail.map((item, index) => ({
          title:
            index === trail.length - 1 ? (
              item.title
            ) : (
              <a onClick={() => history.push(item.path)}>{item.title}</a>
            ),
        }))}
      />
    </div>
  );
};

export default HeaderContent;
