/**
 * 员工编辑/创建页面
 * 负责人：成员 B
 *
 * 功能：分步骤/分组表单，含字段权限控制，支持新建和编辑两种模式
 */

import { getDepartmentTree } from '@/services/organization';
import type { DepartmentTree } from '@/services/organization';
import { getPostList, type Post } from '@/services/organization';
import type {
  Employee,
  EmployeeCreateRequest,
  FieldPermissions,
} from '@/services/employee';
import {
  getEmployeeDetail,
  createEmployee,
  updateEmployee,
  getFieldPermissions,
  generateEmployeeNo,
} from '@/services/employee';
import {
  ArrowLeftOutlined,
  IdcardOutlined,
  PhoneOutlined,
  SaveOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useParams, history } from '@umijs/max';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Input,
  InputNumber,
  message,
  Row,
  Select,
  Space,
  Steps,
  TreeSelect,
} from 'antd';
import dayjs from 'dayjs';
import React, { useEffect, useState } from 'react';

const { Option } = Select;

const GRADE_OPTIONS = [
  'P1', 'P2', 'P3', 'P4', 'P5', 'P6', 'P7', 'P8',
  'M1', 'M2', 'M3',
];

const CONTRACT_TYPE_OPTIONS = [
  { label: '固定期限', value: 1 },
  { label: '无固定期限', value: 2 },
  { label: '劳务合同', value: 3 },
];

const HIRE_TYPE_OPTIONS = [
  { label: '全职', value: 1 },
  { label: '兼职', value: 2 },
  { label: '实习', value: 3 },
];

type Mode = 'create' | 'edit';

const EmployeeEditPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const mode: Mode = id ? 'edit' : 'create';
  const isEdit = mode === 'edit';

  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);

  // 下拉数据
  const [departments, setDepartments] = useState<DepartmentTree[]>([]);
  const [positions, setPositions] = useState<Post[]>([]);
  const [fieldPerms, setFieldPerms] = useState<FieldPermissions | null>(null);

  /** TreeSelect 节点类型 */
  interface TreeNode {
    title: string;
    value: number;
    key: number;
    deptCode: string;
    children?: TreeNode[];
  }

  /** 将部门树递归转为 TreeSelect 的 treeData 格式 */
  const toTreeData = (nodes: DepartmentTree[] | undefined): TreeNode[] => {
    if (!nodes) return [];
    return nodes.map((node) => ({
      title: node.deptName,
      value: node.id,
      key: node.id,
      deptCode: node.deptCode,
      children: toTreeData(node.children),
    }));
  };

  /** 初始化：加载部门树、职位列表、字段权限 */
  useEffect(() => {
    getDepartmentTree()
      .then((data) => {
        console.log('[EmployeeEdit] 部门树数据:', data);
        if (data) setDepartments(data);
      })
      .catch((err) => console.error('[EmployeeEdit] 部门树加载失败:', err));
    getPostList()
      .then((data) => {
        console.log('[EmployeeEdit] 职位列表数据:', data);
        if (data?.records) setPositions(data.records);
      })
      .catch((err) => console.error('[EmployeeEdit] 职位列表加载失败:', err));
    getFieldPermissions()
      .then((data) => {
        console.log('[EmployeeEdit] 字段权限:', data);
        if (data) setFieldPerms(data);
      })
      .catch((err) => console.error('[EmployeeEdit] 字段权限加载失败:', err));
  }, []);

  /** 编辑模式：加载现有数据 */
  useEffect(() => {
    if (!isEdit || !id) return;
    setLoading(true);
    getEmployeeDetail(Number(id))
      .then((data) => {
        if (!data) return;
        form.setFieldsValue({
          ...data,
          birthday: data.birthday ? dayjs(data.birthday) : undefined,
          hireDate: data.hireDate ? dayjs(data.hireDate) : undefined,
          probationEndDate: data.probationEndDate
            ? dayjs(data.probationEndDate)
            : undefined,
          contractExpireDate: data.contractExpireDate
            ? dayjs(data.contractExpireDate)
            : undefined,
        });
      })
      .catch(() => message.error('获取员工信息失败'))
      .finally(() => setLoading(false));
  }, [id, isEdit, form]);

  /** 部门变更时自动生成工号（创建模式） */
  const handleDepartmentChange = async (value: number) => {
    if (isEdit || !value) return;
    // 从部门树中查找 deptCode
    const findDeptCode = (nodes: DepartmentTree[]): string | undefined => {
      for (const node of nodes) {
        if (node.id === value) return node.deptCode;
        if (node.children) {
          const found = findDeptCode(node.children);
          if (found) return found;
        }
      }
      return undefined;
    };
    const deptCode = findDeptCode(departments);
    if (!deptCode) return;
    try {
      const result = await generateEmployeeNo(deptCode);
      if (result?.employeeNo) {
        form.setFieldsValue({ employeeNo: result.employeeNo });
      }
    } catch {
      // 后端不可用时忽略
    }
  };

  /** 判断字段是否可编辑 */
  const isFieldEditable = (fieldName: string): boolean => {
    if (!fieldPerms) return true; // 未加载权限时默认可编辑
    if (fieldPerms.hiddenFields.includes(fieldName)) return false;
    if (fieldPerms.flowFields.includes(fieldName)) return false;
    return true;
  };

  /** 提交 */
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);

      const payload: EmployeeCreateRequest = {
        ...values,
        birthday: values.birthday
          ? dayjs(values.birthday).format('YYYY-MM-DD')
          : undefined,
        hireDate: values.hireDate
          ? dayjs(values.hireDate).format('YYYY-MM-DD')
          : undefined,
        contractExpireDate: values.contractExpireDate
          ? dayjs(values.contractExpireDate).format('YYYY-MM-DD')
          : undefined,
      };

      console.log('[EmployeeEdit] 提交数据:', JSON.stringify(payload, null, 2));

      if (isEdit) {
        await updateEmployee(Number(id), payload);
        message.success('员工信息已更新');
      } else {
        await createEmployee(payload);
        message.success('员工已创建');
      }
      history.push('/employee/list');
    } catch (err: any) {
      console.error('[EmployeeEdit] 提交失败:', err);
      if (err?.errorFields) return; // 表单校验错误，antd 会自动提示
      // 拦截器已弹出具体错误，此处不再重复提示
    } finally {
      setSubmitting(false);
    }
  };

  const steps = [
    { title: '基础信息', icon: <UserOutlined /> },
    { title: '个人信息', icon: <PhoneOutlined /> },
    { title: '工作信息', icon: <TeamOutlined /> },
    { title: '合同信息', icon: <IdcardOutlined /> },
  ];

  return (
    <PageContainer
      onBack={() => history.push('/employee/list')}
      loading={loading}
    >
      <Card style={{ marginBottom: 16 }}>
        <Steps
          current={currentStep}
          onChange={setCurrentStep}
          items={steps.map((s) => ({ title: s.title }))}
          size="small"
        />
      </Card>

      <Form
        form={form}
        layout="vertical"
        initialValues={{
          gender: 1,
          probationMonth: 3,
          hireType: 1,
        }}
      >
        {/* 步骤 0：基础信息 */}
        <Card title="基础信息" style={{ display: currentStep === 0 ? 'block' : 'none' }}>
          <Row gutter={24}>
            <Col span={12}>
              <Form.Item
                name="employeeName"
                label="姓名"
                rules={[{ required: true, message: '请输入姓名' }]}
              >
                <Input placeholder="请输入姓名" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="gender"
                label="性别"
                rules={[{ required: true, message: '请选择性别' }]}
              >
                <Select placeholder="请选择性别">
                  <Option value={1}>男</Option>
                  <Option value={2}>女</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="birthday" label="出生日期">
                <DatePicker style={{ width: '100%' }} placeholder="请选择" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="jobLevel" label="职级">
                <Select placeholder="请选择职级" allowClear>
                  {GRADE_OPTIONS.map((g) => (
                    <Option key={g} value={g}>
                      {g}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>
        </Card>

        {/* 步骤 1：个人信息 */}
        <Card title="个人信息" style={{ display: currentStep === 1 ? 'block' : 'none' }}>
          <Row gutter={24}>
            <Col span={12}>
              <Form.Item
                name="phone"
                label="手机号"
                rules={[{ required: true, message: '请输入手机号' }]}
              >
                <Input
                  placeholder="请输入手机号"
                  disabled={!isFieldEditable('phone')}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="email" label="邮箱">
                <Input
                  placeholder="请输入邮箱"
                  disabled={!isFieldEditable('email')}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="idCardNo"
                label="身份证号"
              >
                <Input
                  placeholder="请输入身份证号"
                  disabled={!isFieldEditable('idCard')}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="emergencyContact" label="紧急联系人">
                <Input placeholder="请输入紧急联系人" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="emergencyPhone" label="紧急联系人电话">
                <Input placeholder="请输入紧急联系人电话" />
              </Form.Item>
            </Col>
          </Row>
        </Card>

        {/* 步骤 2：工作信息 */}
        <Card title="工作信息" style={{ display: currentStep === 2 ? 'block' : 'none' }}>
          <Row gutter={24}>
            {isEdit && (
              <Col span={12}>
                <Form.Item name="employeeNo" label="工号">
                  <Input disabled placeholder="系统自动生成" />
                </Form.Item>
              </Col>
            )}
            <Col span={12}>
              <Form.Item
                name="deptId"
                label="部门"
                rules={[{ required: true, message: '请选择部门' }]}
              >
                <TreeSelect
                  treeData={toTreeData(departments)}
                  placeholder="请选择部门"
                  treeDefaultExpandAll
                  onChange={(v) => handleDepartmentChange(v)}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="postId"
                label="职位"
              >
                <Select
                  placeholder="请选择职位"
                  showSearch
                  optionFilterProp="label"
                  allowClear
                  options={positions.map((p) => ({
                    label: p.postName,
                    value: p.id,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="hireDate"
                label="入职日期"
                rules={[{ required: true, message: '请选择入职日期' }]}
              >
                <DatePicker style={{ width: '100%' }} placeholder="请选择" />
              </Form.Item>
            </Col>
            {!isEdit && (
              <Col span={12}>
                <Form.Item
                  name="probationMonth"
                  label="试用期（月）"
                >
                  <InputNumber
                    min={0}
                    max={12}
                    style={{ width: '100%' }}
                    placeholder="请输入试用期月数"
                  />
                </Form.Item>
              </Col>
            )}
          </Row>
        </Card>

        {/* 步骤 3：合同信息 */}
        <Card title="合同信息" style={{ display: currentStep === 3 ? 'block' : 'none' }}>
          <Row gutter={24}>
            <Col span={12}>
              <Form.Item name="contractType" label="合同类型">
                <Select placeholder="请选择合同类型" allowClear>
                  {CONTRACT_TYPE_OPTIONS.map((o) => (
                    <Option key={o.value} value={o.value}>
                      {o.label}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="contractExpireDate" label="合同到期日">
                <DatePicker style={{ width: '100%' }} placeholder="请选择" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="hireType" label="入职类型">
                <Select placeholder="请选择入职类型" allowClear>
                  {HIRE_TYPE_OPTIONS.map((o) => (
                    <Option key={o.value} value={o.value}>
                      {o.label}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="workLocation" label="工作地点">
                <Input placeholder="请输入工作地点" />
              </Form.Item>
            </Col>
          </Row>
        </Card>
      </Form>

      {/* 底部操作栏 */}
      <Card style={{ marginTop: 16 }}>
        <Row justify="space-between">
          <Col>
            {currentStep > 0 && (
              <Button onClick={() => setCurrentStep(currentStep - 1)}>
                上一步
              </Button>
            )}
          </Col>
          <Col>
            <Space>
              <Button onClick={() => history.push('/employee/list')}>
                取消
              </Button>
              {currentStep < 3 ? (
                <Button type="primary" onClick={() => setCurrentStep(currentStep + 1)}>
                  下一步
                </Button>
              ) : (
                <Button
                  type="primary"
                  icon={<SaveOutlined />}
                  loading={submitting}
                  onClick={handleSubmit}
                >
                  {isEdit ? '保存修改' : '创建员工'}
                </Button>
              )}
            </Space>
          </Col>
        </Row>
      </Card>
    </PageContainer>
  );
};

export default EmployeeEditPage;
