/**
 * 我的申请组件
 */

import React, { useEffect, useState } from 'react';
import { List, Tag, Empty, Spin, Steps } from 'antd';
import { history } from '@umijs/max';
import * as homeService from '@/services/home';
import type { Application } from '@/services/home';

// 状态颜色映射
const statusColorMap: Record<string, string> = {
  APPROVING: 'processing',
  APPROVED: 'success',
  REJECTED: 'error',
};

const MyApplications: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<Application[]>([]);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await homeService.getMyApplications();
      setData(result.records || []);
    } catch (error) {
      console.error('获取我的申请失败', error);
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

  if (data.length === 0) {
    return <Empty description="暂无申请记录" />;
  }

  return (
    <List
      dataSource={data}
      renderItem={(item) => (
        <List.Item>
          <List.Item.Meta
            title={
              <span>
                <Tag color="blue">{item.type}</Tag>
                {item.statusText}
                <Tag color={statusColorMap[item.status]} style={{ marginLeft: 8 }}>
                  {item.statusText}
                </Tag>
              </span>
            }
            description={
              <span>
                提交时间：{item.submitTime}
                {item.status === 'APPROVING' && (
                  <span style={{ marginLeft: 8 }}>当前步骤：{item.currentStep}</span>
                )}
              </span>
            }
          />
        </List.Item>
      )}
    />
  );
};

export default MyApplications;