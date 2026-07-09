/**
 * 待办列表组件
 */

import React, { useEffect, useState } from 'react';
import { List, Tag, Empty, Spin, Button } from 'antd';
import { RightOutlined } from '@ant-design/icons';
import { history } from '@umijs/max';
import * as homeService from '@/services/home';
import type { PendingTask } from '@/services/home';

const PendingList: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<PendingTask[]>([]);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await homeService.getPendingList();
      setData(result.records || []);
    } catch (error) {
      console.error('获取待办列表失败', error);
    } finally {
      setLoading(false);
    }
  };

  const handleClick = (id: number) => {
    history.push(`/approval/detail/${id}`);
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 40 }}>
        <Spin />
      </div>
    );
  }

  if (data.length === 0) {
    return <Empty description="暂无待办任务" />;
  }

  return (
    <List
      dataSource={data}
      renderItem={(item) => (
        <List.Item
          actions={[
            <Button
              key="view"
              type="link"
              icon={<RightOutlined />}
              onClick={() => handleClick(item.id)}
            >
              查看
            </Button>,
          ]}
        >
          <List.Item.Meta
            title={
              <span>
                <Tag color="blue">{item.bizType}</Tag>
                {item.title}
              </span>
            }
            description={
              <span>
                申请人：{item.applicant} | 部门：{item.deptName} | 提交时间：
                {item.submitTime}
              </span>
            }
          />
        </List.Item>
      )}
    />
  );
};

export default PendingList;