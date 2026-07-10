/**
 * 统计卡片组件
 */

import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Spin, Empty } from 'antd';
import {
  TeamOutlined,
  UserAddOutlined,
  CheckCircleOutlined,
  PayCircleOutlined,
  ClockCircleOutlined,
  CalendarOutlined,
} from '@ant-design/icons';
import type { RoleCode } from '@/types/user';
import { getStatCardsByRole, type StatCardConfig } from '@/constants/home';
import * as homeService from '@/services/home';

// 图标映射
const iconMap: Record<string, React.ReactNode> = {
  TeamOutlined: <TeamOutlined />,
  UserAddOutlined: <UserAddOutlined />,
  CheckCircleOutlined: <CheckCircleOutlined />,
  PayCircleOutlined: <PayCircleOutlined />,
  ClockCircleOutlined: <ClockCircleOutlined />,
  CalendarOutlined: <CalendarOutlined />,
};

interface StatCardsProps {
  role: RoleCode;
}

const StatCards: React.FC<StatCardsProps> = ({ role }) => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<Record<string, number>>({});
  const cards = getStatCardsByRole(role);

  useEffect(() => {
    fetchAllData();
  }, [role]);

  const fetchAllData = async () => {
    setLoading(true);
    try {
      const results: Record<string, number> = {};

      // 并行请求所有数据
      await Promise.all(
        cards.map(async (card) => {
          try {
            let result: any;
            switch (card.apiPath) {
              case '/employees/count':
                result = await homeService.getEmployeeCount();
                break;
              case '/employees/count/month-entry':
                result = await homeService.getMonthEntryCount();
                break;
              case '/approval/pending-count':
                result = await homeService.getPendingCount();
                break;
              case '/salary/monthly-total':
                result = await homeService.getMonthlySalaryTotal();
                break;
              case '/my/attendance/summary':
                result = await homeService.getMyAttendanceSummary();
                break;
              case '/my/leave-balance':
                result = await homeService.getMyLeaveBalance();
                break;
              default:
                result = { [card.dataField]: 0 };
            }
            results[card.key] = result[card.dataField] || 0;
          } catch (error) {
            results[card.key] = 0;
          }
        })
      );

      setData(results);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 40 }}>
        <Spin />
      </div>
    );
  }

  if (cards.length === 0) {
    return <Empty description="暂无统计数据" />;
  }

  return (
    <Row gutter={[16, 16]}>
      {cards.map((card) => (
        <Col xs={24} sm={12} lg={6} key={card.key}>
          <Card bordered={false}>
            <Statistic
              title={
                <span>
                  {iconMap[card.icon]} {card.title}
                </span>
              }
              value={data[card.key] || 0}
              suffix={card.unit}
              valueStyle={{ color: card.color }}
            />
          </Card>
        </Col>
      ))}
    </Row>
  );
};

export default StatCards;