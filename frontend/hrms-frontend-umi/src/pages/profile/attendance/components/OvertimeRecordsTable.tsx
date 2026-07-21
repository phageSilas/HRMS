/**
 * 加班记录表格
 */
import { history } from '@umijs/max';
import { Button, Table } from 'antd';
import React from 'react';
import type { OvertimeRecordVO } from '@/services/profile';

interface Props {
  records: OvertimeRecordVO[];
  loading: boolean;
}

const OvertimeRecordsTable: React.FC<Props> = ({ records, loading }) => {
  const columns = [
    {
      title: '加班日期',
      dataIndex: 'overtimeDate',
      key: 'overtimeDate',
      width: 170,
      render: (t: string) => t || '-',
    },
    { title: '时长(小时)', dataIndex: 'duration', key: 'duration', width: 100 },
    { title: '事由', dataIndex: 'reason', key: 'reason', ellipsis: true },
    {
      title: '状态',
      dataIndex: 'approvalStatusDesc',
      key: 'approvalStatusDesc',
      width: 100,
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_: unknown, record: OvertimeRecordVO) =>
        record.approvalInstanceId ? (
          <Button
            type="link"
            size="small"
            onClick={() => history.push(`/approval/detail/${record.approvalInstanceId}`)}
          >
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
      locale={{ emptyText: '暂无加班记录' }}
    />
  );
};

export default OvertimeRecordsTable;
