import GlobalSearch from './GlobalSearch';
import NotificationPanel from './NotificationPanel';
import {
  BellOutlined,
  BgColorsOutlined,
  BulbOutlined,
  FullscreenExitOutlined,
  FullscreenOutlined,
  GlobalOutlined,
  MessageOutlined,
  SearchOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import { Badge, Button, Drawer, Popover, Space, Tooltip, Typography, message } from 'antd';
import React, { useEffect, useState } from 'react';
import styles from './index.less';

const { Text } = Typography;

const HeaderActions: React.FC = () => {
  const [searchOpen, setSearchOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [dark, setDark] = useState(false);
  const [fullscreen, setFullscreen] = useState(false);

  useEffect(() => {
    const savedTheme = localStorage.getItem('hrms-art-theme');
    const nextDark = savedTheme === 'dark';
    setDark(nextDark);
    document.documentElement.classList.toggle('hrms-art-dark', nextDark);
  }, []);

  useEffect(() => {
    const handleKeydown = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault();
        setSearchOpen(true);
      }
    };
    window.addEventListener('keydown', handleKeydown);
    return () => window.removeEventListener('keydown', handleKeydown);
  }, []);

  useEffect(() => {
    const handleFullscreen = () => setFullscreen(Boolean(document.fullscreenElement));
    document.addEventListener('fullscreenchange', handleFullscreen);
    return () => document.removeEventListener('fullscreenchange', handleFullscreen);
  }, []);

  const toggleTheme = () => {
    const nextDark = !dark;
    setDark(nextDark);
    localStorage.setItem('hrms-art-theme', nextDark ? 'dark' : 'light');
    document.documentElement.classList.toggle('hrms-art-dark', nextDark);
  };

  const toggleFullscreen = async () => {
    if (document.fullscreenElement) {
      await document.exitFullscreen();
      return;
    }
    await document.documentElement.requestFullscreen();
  };

  return (
    <div className={styles.headerActions}>
      <button className={styles.searchTrigger} type="button" onClick={() => setSearchOpen(true)}>
        <SearchOutlined />
        <span>搜索</span>
        <kbd>Ctrl K</kbd>
      </button>
      <Tooltip title={fullscreen ? '退出全屏' : '全屏'}>
        <Button
          type="text"
          className={styles.iconButton}
          icon={fullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
          onClick={toggleFullscreen}
        />
      </Tooltip>
      <Tooltip title="简体中文">
        <Button type="text" className={styles.iconButton} icon={<GlobalOutlined />} />
      </Tooltip>
      <Popover
        placement="bottomRight"
        arrow={false}
        trigger="click"
        content={<NotificationPanel />}
        overlayClassName={styles.noticePopover}
      >
        <Badge dot offset={[-7, 8]}>
          <Button type="text" className={styles.iconButton} icon={<BellOutlined />} />
        </Badge>
      </Popover>
      <Tooltip title="AI 消息">
        <Button
          type="text"
          className={styles.iconButton}
          icon={<MessageOutlined />}
          onClick={() => history.push('/ai')}
        />
      </Tooltip>
      <Tooltip title="界面设置">
        <Button
          type="text"
          className={styles.iconButton}
          icon={<SettingOutlined />}
          onClick={() => setSettingsOpen(true)}
        />
      </Tooltip>
      <Tooltip title={dark ? '浅色模式' : '深色模式'}>
        <Button
          type="text"
          className={styles.iconButton}
          icon={dark ? <BgColorsOutlined /> : <BulbOutlined />}
          onClick={toggleTheme}
        />
      </Tooltip>

      <GlobalSearch open={searchOpen} onClose={() => setSearchOpen(false)} />
      <Drawer
        title="界面设置"
        open={settingsOpen}
        onClose={() => setSettingsOpen(false)}
        width={320}
      >
        <Space direction="vertical" size={16}>
          <Text>当前采用 Art Design Pro 风格：浅色侧栏、顶部工具区、圆角卡片和页签栏。</Text>
          <Button onClick={toggleTheme}>{dark ? '切换为浅色模式' : '切换为深色模式'}</Button>
          <Button onClick={() => message.info('更多布局设置后续接入')}>更多设置</Button>
        </Space>
      </Drawer>
    </div>
  );
};

export default HeaderActions;
