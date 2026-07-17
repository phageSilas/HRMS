import ModuleLanding from '@/components/ModuleLanding';
import { ApartmentOutlined } from '@ant-design/icons';
import React from 'react';

/**
 * 组织架构基础页面
 * 作为组织架构模块的入口，展示模块概览
 */
const OrganizationPage: React.FC = () => (
  <ModuleLanding
    title="组织架构"
    description="维护部门树、职位序列、字典数据等组织基础资料，为员工档案、考勤和薪资提供组织维度支撑。"
    icon={<ApartmentOutlined />}
    metrics={[
      { label: '部门层级', value: 4, suffix: '级' },
      { label: '职位序列', value: 6, suffix: '类' },
      { label: '字典类型', value: 3, suffix: '项' },
    ]}
    actions={[
      { label: '部门管理', color: 'blue' },
      { label: '职位管理', color: 'cyan' },
      { label: '字典管理', color: 'green' },
    ]}
  />
);

export default OrganizationPage;
