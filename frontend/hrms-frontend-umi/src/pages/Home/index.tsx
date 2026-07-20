/**
 * 首页工作台
 * 展示天气欢迎横幅、快捷跳转和 AI 助手输入区
 */

import {
  ArrowRightOutlined,
  ArrowUpOutlined,
  EnvironmentOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import { Button, Card, Input, Typography } from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useMemo, useState } from 'react';
import styles from './index.less';
import { DEFAULT_HOME_WEATHER_INFO } from '@/utils/weather';
import { fetchZhengzhouWeather } from '@/services/home/weather';
import type { HomeWeatherInfo, HomeWeatherType } from '@/types/weather';

const { Paragraph, Text, Title } = Typography;
const { TextArea } = Input;

const HOME_AI_PENDING_PROMPT_STORAGE_KEY = 'hrms-ai-pending-prompt';

interface QuickEntry {
  key: string;
  name: string;
  description: string;
  path: string;
  icon: React.ReactNode;
}

interface WeatherIconProps {
  className?: string;
}

/**
 * 渲染晴天图标
 *
 * 本方法使用的工具类: 无
 */
function SunnyIcon({ className }: WeatherIconProps) {
  return (
    <svg className={className} viewBox="0 0 120 120" aria-hidden="true">
      <circle cx="60" cy="60" r="22" className={styles.sunFill} />
      <g className={styles.sunStroke}>
        <line x1="60" y1="10" x2="60" y2="26" />
        <line x1="60" y1="94" x2="60" y2="110" />
        <line x1="10" y1="60" x2="26" y2="60" />
        <line x1="94" y1="60" x2="110" y2="60" />
        <line x1="24" y1="24" x2="36" y2="36" />
        <line x1="84" y1="84" x2="96" y2="96" />
        <line x1="24" y1="96" x2="36" y2="84" />
        <line x1="84" y1="36" x2="96" y2="24" />
      </g>
    </svg>
  );
}

/**
 * 渲染多云图标
 *
 * 本方法使用的工具类: 无
 */
function CloudyIcon({ className }: WeatherIconProps) {
  return (
    <svg className={className} viewBox="0 0 160 120" aria-hidden="true">
      <circle cx="52" cy="42" r="20" className={styles.sunFill} />
      <g className={styles.sunStroke}>
        <line x1="52" y1="10" x2="52" y2="22" />
        <line x1="52" y1="62" x2="52" y2="74" />
        <line x1="20" y1="42" x2="32" y2="42" />
        <line x1="72" y1="42" x2="84" y2="42" />
      </g>
      <path
        className={styles.cloudStroke}
        d="M64 88h50c17 0 30-11 30-26 0-14-10-24-24-26-4-16-18-26-36-26-20 0-36 12-40 31C28 44 16 55 16 70c0 10 6 18 15 18h33Z"
      />
    </svg>
  );
}

/**
 * 渲染阴天图标
 *
 * 本方法使用的工具类: 无
 */
function OvercastIcon({ className }: WeatherIconProps) {
  return (
    <svg className={className} viewBox="0 0 160 120" aria-hidden="true">
      <path
        className={styles.cloudStroke}
        d="M64 88h50c17 0 30-11 30-26 0-14-10-24-24-26-4-16-18-26-36-26-20 0-36 12-40 31C28 44 16 55 16 70c0 10 6 18 15 18h33Z"
      />
      <path
        className={styles.cloudSecondaryStroke}
        d="M54 98h42c14 0 24-9 24-21 0-10-7-18-18-20-3-12-14-20-28-20-15 0-27 9-30 23-12 2-20 11-20 22 0 9 6 16 16 16h14Z"
      />
    </svg>
  );
}

/**
 * 渲染雨天图标
 *
 * 本方法使用的工具类: 无
 */
function RainyIcon({ className }: WeatherIconProps) {
  return (
    <svg className={className} viewBox="0 0 160 140" aria-hidden="true">
      <path
        className={styles.cloudStroke}
        d="M64 88h50c17 0 30-11 30-26 0-14-10-24-24-26-4-16-18-26-36-26-20 0-36 12-40 31C28 44 16 55 16 70c0 10 6 18 15 18h33Z"
      />
      <g className={styles.rainStroke}>
        <line x1="58" y1="98" x2="48" y2="118" />
        <line x1="86" y1="98" x2="76" y2="126" />
        <line x1="114" y1="98" x2="104" y2="118" />
      </g>
    </svg>
  );
}

const QUICK_ENTRIES: QuickEntry[] = [
  {
    key: 'punch',
    name: '打卡',
    description: '进入员工打卡页面，完成上班和下班打卡。',
    path: '/attendance/punch',
    icon: <EnvironmentOutlined />,
  },
  {
    key: 'profile',
    name: '个人中心',
    description: '查看我的档案、考勤、请假、薪资与账号信息。',
    path: '/profile/index',
    icon: <UserOutlined />,
  },
];

const WEATHER_ICON_MAP: Record<
  string,
  React.ComponentType<WeatherIconProps>
> = {
  晴天: SunnyIcon,
  多云: CloudyIcon,
  阴天: OvercastIcon,
  雨天: RainyIcon,
};

/**
 * 规范化天气类型
 *
 * 本方法使用的工具类: 无
 */
function normalizeWeatherType(type: string): HomeWeatherType {
  return (WEATHER_ICON_MAP[type] ? type : '阴天') as HomeWeatherType;
}

/**
 * 获取问候语
 *
 * 本方法使用的工具类: dayjs(dayjs)
 */
function getGreetingText() {
  const hour = dayjs().hour();
  if (hour < 6) {
    return '夜深了';
  }
  if (hour < 11) {
    return '早上好';
  }
  if (hour < 14) {
    return '中午好';
  }
  if (hour < 18) {
    return '下午好';
  }
  return '晚上好';
}

const HomePage: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;
  const [assistantInput, setAssistantInput] = useState('');
  const [weatherInfo, setWeatherInfo] = useState<HomeWeatherInfo>(
    DEFAULT_HOME_WEATHER_INFO,
  );
  const [weatherLoading, setWeatherLoading] = useState(true);
  const [weatherError, setWeatherError] = useState<string | null>(null);

  const weatherType = normalizeWeatherType(weatherInfo.type);
  const WeatherIcon = WEATHER_ICON_MAP[weatherType] || OvercastIcon;

  const welcomeName = useMemo(() => {
    return currentUser?.nickname || currentUser?.username || '同事';
  }, [currentUser?.nickname, currentUser?.username]);

  useEffect(() => {
    let cancelled = false;

    const loadWeather = async () => {
      setWeatherLoading(true);
      const nextWeather = await fetchZhengzhouWeather();

      if (cancelled) {
        return;
      }

      if (nextWeather) {
        setWeatherInfo(nextWeather);
        setWeatherError(null);
      } else {
        setWeatherInfo(DEFAULT_HOME_WEATHER_INFO);
        setWeatherError('获取天气失败，已展示默认天气');
      }

      setWeatherLoading(false);
    };

    void loadWeather();

    return () => {
      cancelled = true;
    };
  }, []);

  /**
   * 发送首页 AI 问题并跳转到助手页
   *
   * 本方法使用的工具类: sessionStorage(Web API),history(@umijs/max),Date(JavaScript 内置)
   */
  const handleAssistantSubmit = () => {
    const content = assistantInput.trim();
    if (!content) {
      return;
    }

    sessionStorage.setItem(
      HOME_AI_PENDING_PROMPT_STORAGE_KEY,
      JSON.stringify({
        content,
        createdAt: new Date().toISOString(),
        source: 'home',
      }),
    );
    setAssistantInput('');
    history.push('/ai');
  };

  /**
   * 处理首页 AI 输入框回车发送
   *
   * 本方法使用的工具类: handleAssistantSubmit(当前文件)
   */
  const handleAssistantKeyDown = (
    event: React.KeyboardEvent<HTMLTextAreaElement>,
  ) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      handleAssistantSubmit();
    }
  };

  return (
    <div className={styles.homePage}>
      <Card
        bordered={false}
        className={styles.weatherBanner}
        data-testid="home-weather-banner"
      >
        <div className={styles.weatherGlow} />
        <div className={styles.weatherWaveTop} />
        <div className={styles.weatherWaveBottom} />
        <div className={styles.bannerContent}>
          <div className={styles.bannerText}>
            <Text className={styles.welcomeText}>
              {getGreetingText()}，{welcomeName}
            </Text>
            <Title level={1} className={styles.bannerTitle}>
              今天是高效工作的一天
            </Title>
            <Paragraph className={styles.bannerDescription}>

            </Paragraph>
          </div>

          <div className={styles.weatherInfo} data-testid="home-weather-type">
            <WeatherIcon className={styles.weatherIcon} />
            <div className={styles.weatherMeta}>
              <div className={styles.weatherTemperature}>
                {weatherInfo.temperature}
              </div>
              <div className={styles.weatherType}>
                {weatherInfo.rawWeatherText || weatherType}
              </div>
              <div className={styles.weatherTip}>
                {weatherLoading
                  ? '正在同步郑州实时天气'
                  : weatherError || weatherInfo.tip}
              </div>
            </div>
          </div>
        </div>
      </Card>

      <section className={styles.quickSection}>
        {QUICK_ENTRIES.map((entry) => (
          <Card
            key={entry.key}
            hoverable
            bordered={false}
            className={styles.quickCard}
            data-testid={`home-quick-entry-${entry.key}`}
            onClick={() => history.push(entry.path)}
          >
            <div className={styles.quickCardHeader}>
              <span className={styles.quickCardIcon}>{entry.icon}</span>
              <ArrowRightOutlined className={styles.quickCardArrow} />
            </div>
            <div className={styles.quickCardBody}>
              <Title level={3} className={styles.quickCardTitle}>
                {entry.name}
              </Title>
              <Paragraph className={styles.quickCardDescription}>
                {entry.description}
              </Paragraph>
            </div>
            <div className={styles.quickCardPreview} aria-hidden="true">
              {entry.key === 'punch' ? (
                <>
                  <span className={styles.previewBadge} />
                  <span className={styles.previewPin} />
                </>
              ) : (
                <>
                  <span className={styles.previewPanel} />
                  <span className={styles.previewAvatar} />
                </>
              )}
            </div>
          </Card>
        ))}
      </section>

      <Card
        bordered={false}
        className={styles.aiCard}
        data-testid="home-ai-assistant"
      >
        <div className={styles.aiHeader}>
          <Title level={2} className={styles.aiTitle}>
            AI业务助手
          </Title>
          <Paragraph className={styles.aiDescription}>
            帮你快速查询业务、整理日程、生成回复。
          </Paragraph>
        </div>
        <div className={styles.aiComposer}>
          <div
            className={styles.aiInputShell}
            data-testid="home-ai-input-shell"
          >
            <TextArea
              value={assistantInput}
              className={styles.aiInput}
              placeholder="输入你的问题..."
              autoSize={{ minRows: 3, maxRows: 3 }}
              maxLength={2000}
              onChange={(event) => setAssistantInput(event.target.value)}
              onKeyDown={handleAssistantKeyDown}
            />
            <Button
              type="text"
              shape="circle"
              className={styles.aiSendButton}
              icon={<ArrowUpOutlined />}
              data-testid="home-ai-send"
              disabled={!assistantInput.trim()}
              onClick={handleAssistantSubmit}
            />
          </div>
          {/* 保留占位列，维持该区域整体位置与卡片结构稳定 */}
          <div className={styles.aiComposerSpacer} aria-hidden="true" />
        </div>
      </Card>
    </div>
  );
};

export default HomePage;
