/**
 * 个人中心首页
 * 个人信息摘要 + 快捷入口宫格 + 申请进度
 */

import {
  ClockCircleOutlined,
  FileTextOutlined,
  PayCircleOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import { Avatar, Card, Col, Row, Space, Spin, Tag, Typography } from 'antd';
import React from 'react';
import { getProfile } from '@/services/profile';
import { useAsyncData } from '@/hooks/useAsyncData';
import styles from './style.less';

const { Paragraph, Text, Title } = Typography;

// ============ 快捷入口配置 ============

interface QuickEntry {
  key: string;
  name: string;
  path: string;
  icon: React.ReactNode;
  color: string;
  bgColor: string;
}

const QUICK_ENTRIES: QuickEntry[] = [
  {
    key: 'archive',
    name: '我的档案',
    path: '/profile/archive',
    icon: <FileTextOutlined />,
    color: '#1677ff',
    bgColor: '#e6f4ff',
  },
  {
    key: 'attendance',
    name: '我的考勤',
    path: '/profile/attendance',
    icon: <ClockCircleOutlined />,
    color: '#fa8c16',
    bgColor: '#fff7e6',
  },
  {
    key: 'leave',
    name: '我的请假',
    path: '/profile/leave',
    icon: <FileTextOutlined />,
    color: '#52c41a',
    bgColor: '#f6ffed',
  },
  {
    key: 'salary',
    name: '我的薪资',
    path: '/profile/salary',
    icon: <PayCircleOutlined />,
    color: '#722ed1',
    bgColor: '#f9f0ff',
  },
  {
    key: 'security',
    name: '账号安全',
    path: '/profile/security',
    icon: <SafetyCertificateOutlined />,
    color: '#eb2f96',
    bgColor: '#fff0f6',
  },
];

// ============ 页面组件 ============

const ProfileIndexPage: React.FC = () => {
  const { data: profile, loading, error } = useAsyncData(() => getProfile());

  return (
    <div className={styles.pageContainer}>
      {/* 个人信息摘要卡片 */}
      <Card bordered={false} className={styles.profileCard}>
        {loading ? (
          <Spin />
        ) : error || !profile ? (
          <Text type="warning">获取个人信息失败，请稍后重试</Text>
        ) : (
          <Row align="middle" gutter={24}>
            <Col>
              <Avatar
                size={72}
                icon={<UserOutlined />}
                style={{ backgroundColor: '#1677ff' }}
              />
            </Col>
            <Col flex="auto">
              <Space direction="vertical" size={4}>
                <Title level={4} className={styles.profileTitle}>
                  {profile.employeeName || '未知'}
                </Title>
                <Space split={<Text type="secondary">|</Text>}>
                  <Text>{profile.employeeNo}</Text>
                  <Text>{profile.deptName}</Text>
                  <Text>{profile.postName}</Text>
                </Space>
                <Space>
                  <Tag icon={<UserOutlined />} color="blue">
                    {profile.gender === 1 ? '男' : profile.gender === 2 ? '女' : '-'}
                  </Tag>
                  <Text type="secondary">入职 {profile.hireDate}</Text>
                </Space>
              </Space>
            </Col>
          </Row>
        )}
      </Card>

      {/* 快捷入口宫格 */}
      <Row gutter={[16, 16]}>
        {QUICK_ENTRIES.map((entry) => (
          <Col xs={12} sm={8} lg={4} key={entry.key}>
            <Card
              hoverable
              bordered={false}
              className={styles.entryCard}
              bodyStyle={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                height: '100%',
              }}
              onClick={() => history.push(entry.path)}
            >
              <div
                className={styles.entryIcon}
                style={{ color: entry.color, backgroundColor: entry.bgColor }}
              >
                {entry.icon}
              </div>
              <Text strong>{entry.name}</Text>
            </Card>
          </Col>
        ))}
      </Row>

      {/* 底部说明 */}
      <Card bordered={false} className={styles.footerCard}>
        <Paragraph type="secondary" className={styles.footerText}>
          个人中心是您的一站式自助入口，可在此查看档案、考勤打卡、请假、薪资和修改密码。
        </Paragraph>
      </Card>
    </div>
  );
};

export default ProfileIndexPage;
