/**
 * 员工详情页面
 * 负责人：成员 B
 *
 * 功能：员工摘要区 + 分组档案（基础信息/个人信息/工作信息/合同与薪资）
 * 敏感字段按权限脱敏显示
 */

import type { Employee } from '@/services/employee';
import { getEmployeeDetail } from '@/services/employee';
import {
  SafetyCertificateOutlined,
  UserOutlined,
  IdcardOutlined,
  PhoneOutlined,
  MailOutlined,
  BankOutlined,
  CalendarOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useParams, history } from '@umijs/max';
import {
  Button,
  Card,
  Col,
  Descriptions,
  Row,
  Skeleton,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import React, { useEffect, useState } from 'react';

const { Title, Text } = Typography;

/** 脱敏显示：保留首尾，中间用 * 替换 */
function maskValue(value: string | undefined | null): string {
  if (!value) return '-';
  if (value.length <= 4) return value;
  return value.slice(0, 1) + '****' + value.slice(-1);
}

/** 在职状态映射（后端返回数字） */
const STATUS_MAP: Record<number, { label: string; color: string }> = {
  1: { label: '试用期', color: 'orange' },
  2: { label: '正式', color: 'blue' },
  3: { label: '待离职', color: 'volcano' },
  4: { label: '已离职', color: 'red' },
};

/** 合同类型映射（后端返回数字） */
const CONTRACT_TYPE_MAP: Record<number, string> = {
  1: '固定期限',
  2: '无固定期限',
  3: '劳务合同',
};

/** 性别映射 */
const GENDER_MAP: Record<number, string> = {
  1: '男',
  2: '女',
};

const EmployeeDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      setLoading(true);
      getEmployeeDetail(Number(id))
        .then((data) => data && setEmployee(data))
        .catch(() => message.error('获取员工详情失败'))
        .finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) {
    return (
      <PageContainer>
        <Skeleton active paragraph={{ rows: 12 }} />
      </PageContainer>
    );
  }

  if (!employee) {
    return (
      <PageContainer>
        <Card>
          <div style={{ textAlign: 'center', padding: 48 }}>
            <Text type="secondary">未找到该员工信息</Text>
            <br />
            <Button
              type="link"
              onClick={() => history.push('/employee/list')}
              style={{ marginTop: 8 }}
            >
              返回员工列表
            </Button>
          </div>
        </Card>
      </PageContainer>
    );
  }

  const statusInfo = STATUS_MAP[Number(employee.employmentStatus)];
  const genderLabel = GENDER_MAP[Number(employee.gender)] || '未知';

  return (
    <PageContainer
      onBack={() => history.push('/employee/list')}
      extra={
        <Button
          type="primary"
          onClick={() => history.push(`/employee/${id}/edit`)}
        >
          编辑
        </Button>
      }
    >
      {/* 员工摘要区 */}
      <Card style={{ marginBottom: 16 }}>
        <Row align="middle" gutter={24}>
          <Col>
            <div
              style={{
                width: 64,
                height: 64,
                borderRadius: '50%',
                background: '#1890ff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
                fontSize: 28,
                fontWeight: 600,
              }}
            >
              {employee.employeeName?.charAt(0) || '?'}
            </div>
          </Col>
          <Col flex="auto">
            <Space size={16} align="baseline">
              <Title level={3} style={{ margin: 0 }}>
                {employee.employeeName}
              </Title>
              <Tag color={statusInfo?.color || 'default'}>
                {statusInfo?.label || employee.employmentStatus}
              </Tag>
            </Space>
            <div style={{ marginTop: 8, color: '#666' }}>
              <Space size={16}>
                <span>
                  <IdcardOutlined /> 工号：{employee.employeeNo}
                </span>
                <span>
                  <TeamOutlined /> {employee.deptName}
                </span>
                <span>
                  <UserOutlined /> {employee.postName}
                </span>
              </Space>
            </div>
          </Col>
        </Row>
      </Card>

      {/* 基础信息 */}
      <Card title="基础信息" style={{ marginBottom: 16 }}>
        <Descriptions column={3} bordered size="small">
          <Descriptions.Item label="姓名">
            {employee.employeeName}
          </Descriptions.Item>
          <Descriptions.Item label="性别">{genderLabel}</Descriptions.Item>
          <Descriptions.Item label="出生日期">
            {employee.birthday || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="民族">-</Descriptions.Item>
          <Descriptions.Item label="婚姻状况">-</Descriptions.Item>
          <Descriptions.Item label="籍贯">-</Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 个人信息 */}
      <Card
        title={
          <Space>
            <PhoneOutlined />
            个人信息
          </Space>
        }
        style={{ marginBottom: 16 }}
      >
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="手机号">
            {maskValue(employee.phone)}
          </Descriptions.Item>
          <Descriptions.Item label="邮箱">
            {employee.email || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="身份证号">
            {maskValue(employee.idCardNo)}
          </Descriptions.Item>
          <Descriptions.Item label="紧急联系人">
            {employee.emergencyContact || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="紧急联系人电话">
            {maskValue(employee.emergencyPhone)}
          </Descriptions.Item>
          <Descriptions.Item label="银行账号">
            {maskValue(employee.bankAccount)}
          </Descriptions.Item>
        </Descriptions>
        <Text
          type="secondary"
          style={{ display: 'block', marginTop: 8, fontSize: 12 }}
        >
          <SafetyCertificateOutlined /> 敏感信息已脱敏展示，如需查看完整信息请联系 HR
        </Text>
      </Card>

      {/* 工作信息 */}
      <Card
        title={
          <Space>
            <TeamOutlined />
            工作信息
          </Space>
        }
        style={{ marginBottom: 16 }}
      >
        <Descriptions column={3} bordered size="small">
          <Descriptions.Item label="工号">
            {employee.employeeNo}
          </Descriptions.Item>
          <Descriptions.Item label="部门">
            {employee.deptName}
          </Descriptions.Item>
          <Descriptions.Item label="职位">
            {employee.postName}
          </Descriptions.Item>
          <Descriptions.Item label="职级">
            {employee.jobLevel || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="入职日期">
            {employee.hireDate || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="试用期到期日">
            {employee.probationEndDate || '-'}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 合同与薪资信息 */}
      <Card
        title={
          <Space>
            <CalendarOutlined />
            合同与薪资信息
          </Space>
        }
      >
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="合同类型">
            {CONTRACT_TYPE_MAP[employee.contractType] || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="合同到期日">
            {employee.contractExpireDate || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="工作地点">
            {employee.workLocation || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="到期提醒">
            {employee.contractExpireDate ? (
              <Tag color="orange">请关注到期</Tag>
            ) : (
              '-'
            )}
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </PageContainer>
  );
};

export default EmployeeDetailPage;
