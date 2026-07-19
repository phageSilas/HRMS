/**
 * 补卡记录表格
 */
import { history } from '@umijs/max';
import { Button, Table, Tag } from 'antd';
import React from 'react';
import { APPROVAL_STATUS_MAP, CORRECTION_TYPE_MAP } from '../constants';
import type { MakeupRecordVO } from '@/services/profile';

interface Props {
  records: MakeupRecordVO[];
  loading: boolean;
}

const MakeupRecordsTable: React.FC<Props> = ({ records, loading }) => {
  const columns = [
    { title: '补卡日期', dataIndex: 'correctionDate', key: 'correctionDate', width: 120 },
    {
      title: '补卡类型',
      dataIndex: 'correctionType',
      key: 'correctionType',
      width: 100,
      render: (t: string) => CORRECTION_TYPE_MAP[t] || t,
    },
    { title: '原因', dataIndex: 'correctionReason', key: 'correctionReason', ellipsis: true },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      key: 'approvalStatus',
      width: 100,
      render: (s: number) => {
        const item = APPROVAL_STATUS_MAP[s] || { text: '未知', color: 'default' };
        return <Tag color={item.color}>{item.text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_: unknown, record: MakeupRecordVO) =>
        record.approvalInstanceId ? (
          <Button type="link" size="small" onClick={() => history.push(`/approval/detail/${record.approvalInstanceId}`)}>
            查看进度
          </Button>
        ) : null,
    },
    {
      title: '申请时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 170,
      render: (t: string) => t || '-',
    },
  ];

  return (
    <Table
      dataSource={records}
      columns={columns}
      rowKey="id"
      loading={loading}
      pagination={false}
      locale={{ emptyText: '暂无补卡记录' }}
    />
  );
};

export default MakeupRecordsTable;
