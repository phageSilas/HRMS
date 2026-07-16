import {
  CalendarOutlined,
  CheckCircleFilled,
  ClockCircleOutlined,
  ExclamationCircleFilled,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { Button, Card, Empty, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import React from 'react';
import styles from './index.less';

const { Text, Title } = Typography;

interface AttendanceRecordRow {
  id: number;
  recordDate: string;
  employeeName: string;
  departmentName: string;
  clockInTime?: string;
  clockOutTime?: string;
  status: string;
}

const statusItems = [
  { label: '正常', desc: '按时打卡，无异常', color: 'success' },
  { label: '迟到', desc: '超出规定时间 15 分钟内', color: 'warning' },
  { label: '早退', desc: '提前 15 分钟内下班', color: 'orange' },
  { label: '旷工半天', desc: '超出阈值超过 15 分钟', color: 'error' },
  { label: '上班缺卡', desc: '上班未打卡，下班有记录', color: 'purple' },
  { label: '下班缺卡', desc: '上班已打卡，下班无记录', color: 'blue' },
];

const columns: ColumnsType<AttendanceRecordRow> = [
  { title: '日期', dataIndex: 'recordDate', width: 150 },
  { title: '姓名', dataIndex: 'employeeName', width: 140 },
  { title: '部门', dataIndex: 'departmentName', width: 150 },
  {
    title: '上班时间',
    dataIndex: 'clockInTime',
    width: 150,
    render: (value?: string) => value || <Text type="danger">--</Text>,
  },
  {
    title: '下班时间',
    dataIndex: 'clockOutTime',
    width: 150,
    render: (value?: string) => value || <Text type="danger">--</Text>,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 140,
    render: (value: string) => {
      const item = statusItems.find((status) => status.label === value);
      return <Tag color={item?.color || 'default'}>{value}</Tag>;
    },
  },
];

const AttendanceRecordPage: React.FC = () => {
  return (
    <PageContainer title={false} className={styles.recordPage}>
      <div className={styles.pageHeader}>
        <div>
          <Title level={3}>考勤记录</Title>
          <Text type="secondary">
            面向 HR、部门主管和管理员查看权限范围内的员工打卡情况
          </Text>
        </div>
        <Button
          type="primary"
          onClick={() => history.push('/profile/attendance')}
        >
          申请补卡（1/2）
        </Button>
      </div>

      <Card bordered={false} className={styles.statusCard}>
        <Title level={5}>打卡状态说明</Title>
        <div className={styles.statusGrid}>
          {statusItems.map((item) => (
            <div className={styles.statusItem} key={item.label}>
              <Tag color={item.color}>{item.label}</Tag>
              <Text type="secondary">{item.desc}</Text>
            </div>
          ))}
        </div>
      </Card>

      <Card
        bordered={false}
        className={styles.tableCard}
        title={
          <Space>
            <CalendarOutlined />
            <span>{dayjs().format('YYYY-MM')} 打卡记录</span>
          </Space>
        }
        extra={
          <Space className={styles.tableHint}>
            <ClockCircleOutlined />
            <Text type="secondary">等待后端管理端分页接口</Text>
          </Space>
        }
      >
        <Table<AttendanceRecordRow>
          rowKey="id"
          columns={columns}
          dataSource={[]}
          pagination={false}
          locale={{
            emptyText: (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description={
                  <div className={styles.emptyText}>
                    <Space>
                      <ExclamationCircleFilled />
                      <Text strong>暂未接入管理端考勤记录分页接口</Text>
                    </Space>
                    <p>
                      建议后端提供 <code>GET /api/v1/attendance/records</code>
                      ，支持按日期、员工、部门和状态分页查询。
                    </p>
                    <p>
                      建议参数：
                      <code>
                        pageNum、pageSize、startDate、endDate、employeeName、departmentId、status
                      </code>
                    </p>
                  </div>
                }
              />
            ),
          }}
        />
      </Card>

      <Card bordered={false} className={styles.nextCard}>
        <Space align="start">
          <CheckCircleFilled className={styles.nextIcon} />
          <div>
            <Text strong>后续接入方式</Text>
            <p>
              当前页面已保留表格列、状态 Tag 和筛选扩展位置。后端补齐多人记录分页接口后，
              前端只需把表格 dataSource 替换为接口返回的 records，并接入分页 total。
            </p>
          </div>
        </Space>
      </Card>
    </PageContainer>
  );
};

export default AttendanceRecordPage;
