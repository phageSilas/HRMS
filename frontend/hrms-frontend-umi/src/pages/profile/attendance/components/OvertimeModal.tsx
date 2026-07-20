/**
 * 加班申请弹窗
 */
import { DatePicker, Form, Input, InputNumber, Modal } from 'antd';
import React from 'react';

interface Props {
  open: boolean;
  onClose: () => void;
  onSubmit: (values: any) => Promise<void>;
}

const OvertimeModal: React.FC<Props> = ({ open, onClose, onSubmit }) => {
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
      title="申请加班"
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="overtimeDate"
          label="加班日期"
          rules={[{ required: true, message: '请选择加班日期' }]}
        >
          <DatePicker showTime style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item
          name="duration"
          label="加班时长(小时)"
          rules={[{ required: true, message: '请输入加班时长' }]}
        >
          <InputNumber min={0.5} max={24} step={0.5} style={{ width: '100%' }} placeholder="请输入加班时长" />
        </Form.Item>
        <Form.Item
          name="reason"
          label="加班事由"
          rules={[{ required: true, message: '请输入加班事由' }]}
        >
          <Input.TextArea rows={3} placeholder="请输入加班事由" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default OvertimeModal;
