import { Card, Col, Row, Space, Statistic, Tag, Typography } from 'antd';
import type { ReactNode } from 'react';
import React from 'react';
import styles from './index.less';

const { Paragraph, Text, Title } = Typography;

export interface ModuleMetric {
  label: string;
  value: string | number;
  suffix?: string;
}

export interface ModuleAction {
  label: string;
  color?: string;
}

export interface ModuleLandingProps {
  title: string;
  description: string;
  icon: ReactNode;
  metrics: ModuleMetric[];
  actions: ModuleAction[];
}

/**
 * 基础模块落地页，用于尝试性生成阶段承载九大模块的统一占位界面
 */
const ModuleLanding: React.FC<ModuleLandingProps> = ({
  title,
  description,
  icon,
  metrics,
  actions,
}) => {
  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <div className={styles.icon}>{icon}</div>
        <div>
          <Title level={3}>{title}</Title>
          <Paragraph>{description}</Paragraph>
        </div>
      </div>

      <Row gutter={[16, 16]}>
        {metrics.map((metric) => (
          <Col xs={24} sm={12} lg={8} key={metric.label}>
            <Card bordered={false} className={styles.metricCard}>
              <Statistic
                title={metric.label}
                value={metric.value}
                suffix={metric.suffix}
              />
            </Card>
          </Col>
        ))}
      </Row>

      <Card bordered={false} className={styles.workbench} title="基础功能入口">
        <Space size={[8, 12]} wrap>
          {actions.map((action) => (
            <Tag color={action.color || 'blue'} key={action.label}>
              {action.label}
            </Tag>
          ))}
        </Space>
        <Text type="secondary" className={styles.tip}>
          当前为尝试性生成的基础界面，后续按模块逐步接入真实接口、表格、表单和审批流。
        </Text>
      </Card>
    </div>
  );
};

export default ModuleLanding;
