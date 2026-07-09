/**
 * 首页工作台
 * 显示统计卡片、待办列表、我的申请
 */

import React from 'react';
import { Row, Col, Card, Typography } from 'antd';
import { useModel, useAccess } from '@umijs/max';
import StatCards from './components/StatCards';
import PendingList from './components/PendingList';
import MyApplications from './components/MyApplications';
import type { RoleCode } from '@/types/user';

const { Title } = Typography;

const HomePage: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const access = useAccess();

  const currentUser = initialState?.currentUser;
  const roleCode = currentUser?.roleCode as RoleCode;

  // 判断是否显示待办列表（管理员、HR、主管、财务）
  const showPendingList = access.canApprove;

  // 判断是否显示我的申请（普通员工）
  const showMyApplications = roleCode === 'EMPLOYEE';

  return (
    <div style={{ padding: 24 }}>
      {/* 欢迎语 */}
      <Title level={4} style={{ marginBottom: 24 }}>
        欢迎回来，{currentUser?.nickname || currentUser?.username}
      </Title>

      {/* 统计卡片 */}
      <StatCards role={roleCode} />

      {/* 待办列表 / 我的申请 */}
      <Row gutter={24} style={{ marginTop: 24 }}>
        {showPendingList && (
          <Col xs={24} lg={12}>
            <Card title="待办任务" bordered={false}>
              <PendingList />
            </Card>
          </Col>
        )}
        {showMyApplications && (
          <Col xs={24} lg={12}>
            <Card title="我的申请" bordered={false}>
              <MyApplications />
            </Card>
          </Col>
        )}
      </Row>
    </div>
  );
};

export default HomePage;