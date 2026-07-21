/**
 * 字典管理页面
 * 功能：字典类型管理、字典数据管理
 */

import React, { useState, useRef, useEffect } from 'react';
import { PageContainer, ProTable, ProColumns } from '@ant-design/pro-components';
import {
  Button,
  Card,
  Col,
  Form,
  Input,
  message,
  Modal,
  Popconfirm,
  Row,
  Space,
  Table,
  Tag,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  BookOutlined,
  DatabaseOutlined,
} from '@ant-design/icons';
import {
  getDictTypeList,
  getDictDataList,
  createDictType,
  updateDictType,
  deleteDictType,
  createDictData,
  updateDictData,
  deleteDictData,
} from '@/services/organization';
import type { DictTypeItem, DictDataItem } from '@/services/organization';

const { TextArea } = Input;

const DictPage: React.FC = () => {
  // 字典类型相关状态
  const [typeList, setTypeList] = useState<DictTypeItem[]>([]);
  const [typeLoading, setTypeLoading] = useState(false);
  const [typeModalVisible, setTypeModalVisible] = useState(false);
  const [typeModalTitle, setTypeModalTitle] = useState('新增字典类型');
  const [currentType, setCurrentType] = useState<DictTypeItem | null>(null);
  const [typeForm] = Form.useForm();
  const typeActionRef = useRef<any>();

  // 字典数据相关状态
  const [dataList, setDataList] = useState<DictDataItem[]>([]);
  const [dataLoading, setDataLoading] = useState(false);
  const [selectedTypeCode, setSelectedTypeCode] = useState<string>('');
  const [dataModalVisible, setDataModalVisible] = useState(false);
  const [dataModalTitle, setDataModalTitle] = useState('新增字典数据');
  const [currentData, setCurrentData] = useState<DictDataItem | null>(null);
  const [dataForm] = Form.useForm();

  // 加载字典类型列表
  const fetchTypeList = async () => {
    setTypeLoading(true);
    try {
      const res = await getDictTypeList({ pageNum: 1, pageSize: 1000 });
      setTypeList(res.records || []);
    } catch (error: any) {
      message.error(error.message || '获取字典类型列表失败');
    } finally {
      setTypeLoading(false);
    }
  };

  // 加载字典数据列表
  const fetchDataList = async (typeCode: string) => {
    if (!typeCode) return;
    setDataLoading(true);
    try {
      const data = await getDictDataList(typeCode);
      setDataList(data || []);
    } catch (error: any) {
      message.error(error.message || '获取字典数据失败');
    } finally {
      setDataLoading(false);
    }
  };

  useEffect(() => {
    fetchTypeList();
  }, []);

  // 选择字典类型
  const handleSelectType = (typeCode: string) => {
    setSelectedTypeCode(typeCode);
    fetchDataList(typeCode);
  };

  // 状态标签
  const statusTag = (status: number) => {
    const map: Record<number, { text: string; color: string }> = {
      0: { text: '禁用', color: 'red' },
      1: { text: '启用', color: 'green' },
    };
    const item = map[status] || { text: '未知', color: 'default' };
    return <Tag color={item.color}>{item.text}</Tag>;
  };

  // ============ 字典类型操作 ============

  // 打开新增字典类型弹窗
  const handleAddType = () => {
    setTypeModalTitle('新增字典类型');
    setCurrentType(null);
    typeForm.resetFields();
    setTypeModalVisible(true);
  };

  // 打开编辑字典类型弹窗
  const handleEditType = (record: DictTypeItem) => {
    setTypeModalTitle('编辑字典类型');
    setCurrentType(record);
    typeForm.setFieldsValue({
      dictName: record.dictName,
      dictType: record.dictType,
      remark: record.remark,
    });
    setTypeModalVisible(true);
  };

  // 删除字典类型
  const handleDeleteType = async (id: number) => {
    try {
      await deleteDictType(id);
      message.success('删除成功');
      fetchTypeList();
      // 如果删除的是当前选中的类型，清空数据列表
      if (currentType?.id === id) {
        setDataList([]);
        setSelectedTypeCode('');
      }
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 提交字典类型表单
  const handleTypeSubmit = async () => {
    try {
      const values = await typeForm.validateFields();
      if (currentType) {
        await updateDictType(currentType.id, values);
        message.success('更新成功');
      } else {
        await createDictType(values);
        message.success('创建成功');
      }
      setTypeModalVisible(false);
      typeForm.resetFields();
      fetchTypeList();
    } catch (error: any) {
      message.error(error.message || '操作失败');
    }
  };

  // ============ 字典数据操作 ============

  // 打开新增字典数据弹窗
  const handleAddData = () => {
    if (!selectedTypeCode) {
      message.warning('请先选择字典类型');
      return;
    }
    setDataModalTitle('新增字典数据');
    setCurrentData(null);
    dataForm.resetFields();
    dataForm.setFieldsValue({ dictType: selectedTypeCode });
    setDataModalVisible(true);
  };

  // 打开编辑字典数据弹窗
  const handleEditData = (record: DictDataItem) => {
    setDataModalTitle('编辑字典数据');
    setCurrentData(record);
    dataForm.setFieldsValue({
      dictType: record.dictType,
      dictLabel: record.dictLabel,
      dictValue: record.dictValue,
      cssClass: record.cssClass,
      sort: record.sort,
      remark: record.remark,
    });
    setDataModalVisible(true);
  };

  // 删除字典数据
  const handleDeleteData = async (id: number) => {
    try {
      await deleteDictData(id);
      message.success('删除成功');
      fetchDataList(selectedTypeCode);
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 提交字典数据表单
  const handleDataSubmit = async () => {
    try {
      const values = await dataForm.validateFields();
      if (currentData) {
        await updateDictData(currentData.id, values);
        message.success('更新成功');
      } else {
        await createDictData(values);
        message.success('创建成功');
      }
      setDataModalVisible(false);
      dataForm.resetFields();
      fetchDataList(selectedTypeCode);
    } catch (error: any) {
      message.error(error.message || '操作失败');
    }
  };

  // 字典类型表格列
  const typeColumns = [
    {
      title: '字典名称',
      dataIndex: 'dictName',
      key: 'dictName',
      width: 150,
    },
    {
      title: '字典类型编码',
      dataIndex: 'dictType',
      key: 'dictType',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => statusTag(status),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true,
      render: (text: string) => text || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 140,
      fixed: 'right' as const,
      render: (_: any, record: DictTypeItem) => (
        <Space size={4}>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEditType(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除"
            description={`确定要删除字典类型 "${record.dictName}" 吗？`}
            onConfirm={() => handleDeleteType(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger size="small" icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 字典数据表格列
  const dataColumns = [
    {
      title: '字典标签',
      dataIndex: 'dictLabel',
      key: 'dictLabel',
      width: 150,
    },
    {
      title: '字典值',
      dataIndex: 'dictValue',
      key: 'dictValue',
      width: 150,
    },
    {
      title: '排序号',
      dataIndex: 'sort',
      key: 'sort',
      width: 80,
    },
    {
      title: '样式属性',
      dataIndex: 'cssClass',
      key: 'cssClass',
      width: 120,
      render: (text: string) => text || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => statusTag(status),
    },
    {
      title: '操作',
      key: 'action',
      width: 140,
      fixed: 'right' as const,
      render: (_: any, record: DictDataItem) => (
        <Space size={4}>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEditData(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除"
            description={`确定要删除字典数据 "${record.dictLabel}" 吗？`}
            onConfirm={() => handleDeleteData(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger size="small" icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer title="字典管理">
      <Row gutter={16}>
        {/* 左侧字典类型 */}
        <Col span={10}>
          <Card
            title={
              <span>
                <BookOutlined style={{ marginRight: 8 }} />
                字典类型
              </span>
            }
            extra={
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAddType}>
                新增类型
              </Button>
            }
          >
            <Table
              dataSource={typeList}
              columns={typeColumns}
              rowKey="id"
              loading={typeLoading}
              pagination={false}
              size="small"
              rowClassName={(record) =>
                selectedTypeCode === record.dictType ? 'ant-table-row-selected' : ''
              }
              onRow={(record) => ({
                onClick: () => handleSelectType(record.dictType),
                style: {
                  cursor: 'pointer',
                  backgroundColor: selectedTypeCode === record.dictType ? '#e6f7ff' : undefined,
                },
              })}
            />
          </Card>
        </Col>

        {/* 右侧字典数据 */}
        <Col span={14}>
          <Card
            title={
              <span>
                <DatabaseOutlined style={{ marginRight: 8 }} />
                字典数据
                {selectedTypeCode && (
                  <Tag color="blue" style={{ marginLeft: 8 }}>
                    {typeList.find((t) => t.dictType === selectedTypeCode)?.dictName || selectedTypeCode}
                  </Tag>
                )}
              </span>
            }
            extra={
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAddData}>
                新增数据
              </Button>
            }
          >
            {selectedTypeCode ? (
              <Table
                dataSource={dataList}
                columns={dataColumns}
                rowKey="id"
                loading={dataLoading}
                pagination={false}
                size="small"
              />
            ) : (
              <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
                <BookOutlined style={{ fontSize: 48, marginBottom: 16, color: '#d9d9d9' }} />
                <p>请先选择左侧字典类型</p>
              </div>
            )}
          </Card>
        </Col>
      </Row>

      {/* 字典类型弹窗 */}
      <Modal
        title={typeModalTitle}
        open={typeModalVisible}
        onOk={handleTypeSubmit}
        onCancel={() => {
          setTypeModalVisible(false);
          typeForm.resetFields();
        }}
        width={500}
      >
        <Form form={typeForm} layout="vertical">
          <Form.Item
            name="dictName"
            label="字典名称"
            rules={[{ required: true, message: '请输入字典名称' }]}
          >
            <Input placeholder="请输入字典名称" />
          </Form.Item>
          <Form.Item
            name="dictType"
            label="字典类型编码"
            rules={[{ required: true, message: '请输入字典类型编码' }]}
          >
            <Input placeholder="请输入字典类型编码，如：post_sequence" disabled={!!currentType} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 字典数据弹窗 */}
      <Modal
        title={dataModalTitle}
        open={dataModalVisible}
        onOk={handleDataSubmit}
        onCancel={() => {
          setDataModalVisible(false);
          dataForm.resetFields();
        }}
        width={500}
      >
        <Form form={dataForm} layout="vertical">
          <Form.Item name="dictType" label="字典类型" hidden>
            <Input />
          </Form.Item>
          <Form.Item
            name="dictLabel"
            label="字典标签"
            rules={[{ required: true, message: '请输入字典标签' }]}
          >
            <Input placeholder="请输入字典标签，如：管理序列" />
          </Form.Item>
          <Form.Item
            name="dictValue"
            label="字典值"
            rules={[{ required: true, message: '请输入字典值' }]}
          >
            <Input placeholder="请输入字典值，如：M" />
          </Form.Item>
          <Form.Item name="sort" label="排序号" initialValue={1}>
            <Input type="number" placeholder="请输入排序号" />
          </Form.Item>
          <Form.Item name="cssClass" label="样式属性">
            <Input placeholder="请输入样式属性" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default DictPage;
