/**
 * 补卡申请弹窗
 */
import { DatePicker, Form, Input, Modal, Select, TimePicker, message } from 'antd';
import dayjs from 'dayjs';
import React from 'react';

interface Props {
  open: boolean;
  onClose: () => void;
  onSubmit: (values: any) => Promise<void>;
  initialDate?: dayjs.Dayjs;
}

const MakeupModal: React.FC<Props> = ({ open, onClose, onSubmit, initialDate }) => {
  const [form] = Form.useForm();

  // 每次打开时预填日期
  React.useEffect(() => {
    if (open && initialDate) {
      form.setFieldsValue({ correctionDate: initialDate });
    }
  }, [open, initialDate, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      await onSubmit(values);
      form.resetFields();
    } catch (err: any) {
      // 表单校验失败或提交异常
      if (err?.message) message.error(err.message);
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onClose();
  };

  return (
    <Modal
      title="申请补卡"
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="correctionDate"
          label="补卡日期"
          rules={[{ required: true, message: '请选择补卡日期' }]}
        >
          <DatePicker style={{ width: '100%' }}
            disabledDate={(current) => current && current.isAfter(dayjs().endOf('day'))}
          />
        </Form.Item>
        <Form.Item
          name="correctionTime"
          label="补卡时间"
          rules={[{ required: true, message: '请选择补卡时间' }]}
        >
          <TimePicker style={{ width: '100%' }} format="HH:mm:ss" minuteStep={1} secondStep={30} />
        </Form.Item>
        <Form.Item
          name="correctionType"
          label="补卡类型"
          rules={[{ required: true, message: '请选择补卡类型' }]}
        >
          <Select>
            <Select.Option value="CLOCK_IN">上班卡</Select.Option>
            <Select.Option value="CLOCK_OUT">下班卡</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item
          name="correctionReason"
          label="补卡原因"
          rules={[{ required: true, message: '请输入补卡原因' }]}
        >
          <Input.TextArea rows={3} placeholder="请输入补卡原因" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default MakeupModal;
