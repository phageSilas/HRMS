/**
 * 我的薪资页面
 * 薪资概览 + 工资条列表（跳转至薪资管理模块）
 */
import { RightCircleOutlined } from '@ant-design/icons';
import { history } from '@umijs/max';
import { PageContainer } from '@ant-design/pro-components';
import { Button, Card, Empty, Space, Typography } from 'antd';
import React from 'react';

const { Paragraph, Text, Title } = Typography;

const ProfileSalaryPage: React.FC = () => {
  return (
    <PageContainer>
      <Card bordered={false} style={{ borderRadius: 8, textAlign: 'center', padding: '48px 0' }}>
        <Space direction="vertical" size={20}>
          <Title level={4}>薪资信息</Title>
          <Paragraph type="secondary">
            您的薪资信息在薪资管理模块中查看和管理。
            <br />
            工资条查看需进行二次身份验证。
          </Paragraph>
          <Button
            type="primary"
            size="large"
            icon={<RightCircleOutlined />}
            onClick={() => history.push('/salary/payslip')}
          >
            前往查看工资条
          </Button>
        </Space>
      </Card>
    </PageContainer>
  );
};

export default ProfileSalaryPage;
