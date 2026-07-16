import { getRouteMeta } from './routeMeta';
import { CloseOutlined, DownOutlined, HomeOutlined } from '@ant-design/icons';
import { history, useLocation } from '@umijs/max';
import { Button, Dropdown, Space } from 'antd';
import type { MenuProps } from 'antd';
import React, { useEffect, useMemo, useState } from 'react';
import styles from './index.less';

interface WorkTab {
  path: string;
  title: string;
  icon?: React.ReactNode;
  fixed?: boolean;
}

type StoredWorkTab = Omit<WorkTab, 'icon'>;

const homeTab: WorkTab = {
  path: '/home',
  title: '首页',
  icon: <HomeOutlined />,
  fixed: true,
};

function readTabs(): WorkTab[] {
  try {
    const text = sessionStorage.getItem('hrms-work-tabs');
    if (!text) return [homeTab];
    const parsed = JSON.parse(text) as StoredWorkTab[];
    if (parsed.length === 0) return [homeTab];
    return parsed.map((tab) => {
      const meta = getRouteMeta(tab.path);
      return {
        path: tab.path,
        title: tab.title || meta.title,
        fixed: tab.fixed || meta.fixed,
        icon: meta.icon,
      };
    });
  } catch {
    return [homeTab];
  }
}

const WorkTabs: React.FC = () => {
  const location = useLocation();
  const [tabs, setTabs] = useState<WorkTab[]>(readTabs);
  const activePath = location.pathname === '/' ? '/home' : location.pathname;

  useEffect(() => {
    if (activePath === '/login' || activePath === '/403') return;
    const meta = getRouteMeta(activePath);
    setTabs((current) => {
      if (current.some((tab) => tab.path === activePath)) return current;
      return [
        ...current,
        {
          path: activePath,
          title: meta.title,
          icon: meta.icon,
          fixed: meta.fixed,
        },
      ];
    });
  }, [activePath]);

  useEffect(() => {
    const storedTabs: StoredWorkTab[] = tabs.map(({ path, title, fixed }) => ({
      path,
      title,
      fixed,
    }));
    sessionStorage.setItem('hrms-work-tabs', JSON.stringify(storedTabs));
  }, [tabs]);

  const closeTab = (path: string) => {
    setTabs((current) => {
      const targetIndex = current.findIndex((tab) => tab.path === path);
      const target = current[targetIndex];
      if (!target || target.fixed) return current;

      const nextTabs = current.filter((tab) => tab.path !== path);
      if (path === activePath) {
        const nextActive = nextTabs[targetIndex - 1] || nextTabs[targetIndex] || homeTab;
        history.push(nextActive.path);
      }
      return nextTabs.length > 0 ? nextTabs : [homeTab];
    });
  };

  const closeOtherTabs = () => {
    setTabs((current) => current.filter((tab) => tab.fixed || tab.path === activePath));
  };

  const closeAllTabs = () => {
    setTabs([homeTab]);
    history.push('/home');
  };

  const menuItems = useMemo<MenuProps['items']>(
    () => [
      { key: 'close-current', label: '关闭当前' },
      { key: 'close-other', label: '关闭其他' },
      { key: 'close-all', label: '关闭全部' },
    ],
    []
  );

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'close-current') {
      closeTab(activePath);
    }
    if (key === 'close-other') {
      closeOtherTabs();
    }
    if (key === 'close-all') {
      closeAllTabs();
    }
  };

  return (
    <div className={styles.workTabs}>
      <div className={styles.tabScroller}>
        {tabs.map((tab) => {
          const active = tab.path === activePath;
          return (
            <button
              type="button"
              key={tab.path}
              className={`${styles.workTab} ${active ? styles.workTabActive : ''}`}
              onClick={() => history.push(tab.path)}
            >
              <Space size={6}>
                {tab.icon}
                <span>{tab.title}</span>
                {!tab.fixed && tabs.length > 1 && (
                  <CloseOutlined
                    className={styles.closeTab}
                    onClick={(event) => {
                      event.stopPropagation();
                      closeTab(tab.path);
                    }}
                  />
                )}
              </Space>
            </button>
          );
        })}
      </div>
      <Dropdown menu={{ items: menuItems, onClick: handleMenuClick }} trigger={['click']}>
        <Button className={styles.tabMenuButton} icon={<DownOutlined />} />
      </Dropdown>
    </div>
  );
};

export default WorkTabs;
