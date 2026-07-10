/**
 * 403 无权限页面
 */

import React from 'react';
import { Button, Result } from 'antd';
import { history } from '@umijs/max';

const ForbiddenPage: React.FC = () => {
  return (
    <Result
      status="403"
      title="403"
      subTitle="抱歉，您没有权限访问此页面"
      extra={
        <Button type="primary" onClick={() => history.push('/home')}>
          返回首页
        </Button>
      }
    />
  );
};

export default ForbiddenPage;