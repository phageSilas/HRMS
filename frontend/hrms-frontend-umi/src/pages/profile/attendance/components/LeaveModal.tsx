/**
 * 请假申请弹窗
 */
import { DatePicker, Form, Input, Modal, Select } from 'antd';
import dayjs from 'dayjs';
import React from 'react';
import { LEAVE_TYPE_OPTIONS } from '../constants';

interface Props {
  open: boolean;
  onClose: () => void;
  onSubmit: (values: any) => Promise<void>;
}

const LeaveModal: React.FC<Props> = ({ open, onClose, onSubmit }) => {
  const [form] = Form.useForm();

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      await onSubmit(values);
      form.resetFields();
    } catch {
      // 校验失败或提交异常
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onClose();
  };

  return (
    <Modal
      title="申请请假"
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      width={560}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="leaveType"
          label="请假类型"
          rules={[{ required: true, message: '请选择请假类型' }]}
        >
          <Select placeholder="请选择请假类型">
            {LEAVE_TYPE_OPTIONS.map((opt) => (
              <Select.Option key={opt.value} value={opt.value}>
                {opt.label}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item
          name="dateRange"
          label="请假时间"
          rules={[{ required: true, message: '请选择请假时间范围' }]}
        >
          <DatePicker.RangePicker showTime style={{ width: '100%' }} format="YYYY-MM-DD HH:mm"
            disabledDate={(current) => current && current.isAfter(dayjs().endOf('day'))}
          />
        </Form.Item>
        <Form.Item
          name="leaveReason"
          label="请假事由"
          rules={[{ required: true, message: '请输入请假事由' }]}
        >
          <Input.TextArea rows={3} placeholder="请输入请假事由" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default LeaveModal;
