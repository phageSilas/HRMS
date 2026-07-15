/**
 * 首页工作台
 * 展示九大业务模块的基础跳转入口
 */

import {
  ApartmentOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  PayCircleOutlined,
  RobotOutlined,
  SafetyCertificateOutlined,
  SwapOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import { Card, Col, Row, Space, Tag, Typography } from 'antd';
import React from 'react';

const { Paragraph, Text, Title } = Typography;

interface ModuleEntry {
  name: string;
  path: string;
  description: string;
  owner: string;
  icon: React.ReactNode;
  color: string;
}

const moduleEntries: ModuleEntry[] = [
  {
    name: '权限体系',
    path: '/system/user',
    description: '用户、角色、菜单和按钮权限的基础管理。',
    owner: '系统底座',
    icon: <SafetyCertificateOutlined />,
    color: 'blue',
  },
  {
    name: '组织架构',
    path: '/organization',
    description: '部门树、职位序列和组织基础资料维护。',
    owner: '系统底座',
    icon: <ApartmentOutlined />,
    color: 'cyan',
  },
  {
    name: '员工档案',
    path: '/employee/list',
    description: '员工基础信息、合同和字段权限展示入口。',
    owner: 'HR 高频',
    icon: <TeamOutlined />,
    color: 'green',
  },
  {
    name: '入转调离',
    path: '/process/entry',
    description: '入职、转正、调岗、离职流程入口。',
    owner: '你负责',
    icon: <SwapOutlined />,
    color: 'geekblue',
  },
  {
    name: '考勤管理',
    path: '/attendance/record',
    description: '打卡、请假、补卡、考勤统计的基础入口。',
    owner: '你负责',
    icon: <ClockCircleOutlined />,
    color: 'orange',
  },
  {
    name: '薪资管理',
    path: '/salary/account',
    description: '账套、批次、工资条和薪资看板入口。',
    owner: '你负责',
    icon: <PayCircleOutlined />,
    color: 'purple',
  },
  {
    name: '审批中心',
    path: '/approval/pending',
    description: '待办、已办、审批详情和流程处理。',
    owner: '流程中台',
    icon: <CheckCircleOutlined />,
    color: 'lime',
  },
  {
    name: '个人中心',
    path: '/profile/index',
    description: '我的档案、考勤、请假、薪资和账号安全。',
    owner: '员工自助',
    icon: <UserOutlined />,
    color: 'gold',
  },
  {
    name: 'AI 智能助手',
    path: '/ai',
    description: '制度问答、操作引导和知识库管理入口。',
    owner: '增强能力',
    icon: <RobotOutlined />,
    color: 'magenta',
  },
];

const HomePage: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  return (
    <div style={{ padding: 24 }}>
      <Card bordered={false} style={{ marginBottom: 16, borderRadius: 8 }}>
        <Space direction="vertical" size={4}>
          <Title level={3} style={{ margin: 0 }}>
            HRMS 工作台
          </Title>
          <Text type="secondary">
            欢迎回来，
            {currentUser?.nickname || currentUser?.username || '管理员'}
            。这里是尝试性生成的九大模块基础跳转界面。
          </Text>
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {moduleEntries.map((entry) => (
          <Col xs={24} sm={12} xl={8} key={entry.name}>
            <Card
              hoverable
              bordered={false}
              data-testid={`module-entry-${entry.name}`}
              onClick={() => history.push(entry.path)}
              style={{ height: '100%', borderRadius: 8 }}
            >
              <Space align="start" size={14}>
                <div
                  style={{
                    display: 'grid',
                    width: 44,
                    height: 44,
                    borderRadius: 8,
                    color: '#1677ff',
                    fontSize: 24,
                    background: '#e6f4ff',
                    placeItems: 'center',
                  }}
                >
                  {entry.icon}
                </div>
                <Space direction="vertical" size={6}>
                  <Space>
                    <Title level={5} style={{ margin: 0 }}>
                      {entry.name}
                    </Title>
                    <Tag color={entry.color}>{entry.owner}</Tag>
                  </Space>
                  <Paragraph style={{ marginBottom: 0 }} type="secondary">
                    {entry.description}
                  </Paragraph>
                </Space>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
};

export default HomePage;
