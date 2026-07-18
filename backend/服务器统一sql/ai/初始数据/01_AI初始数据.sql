-- AI 模块初始数据
USE `hrms`;

INSERT INTO `hr_ai_conversation` (`id`, `user_id`, `session_token`, `title`, `status`, `message_count`, `create_by`, `update_by`, `version`, `create_time`, `update_time`, `is_deleted`) VALUES
(70001, 1, NULL, '管理员咨询考勤规则', 1, 2, 1, 1, 0, NOW(), NOW(), 0),
(70002, 14001, NULL, '普通员工咨询工资条', 1, 2, 14001, 14001, 0, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `status` = VALUES(`status`), `message_count` = VALUES(`message_count`), `update_by` = VALUES(`update_by`), `update_time` = VALUES(`update_time`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `hr_ai_message` (`id`, `conversation_id`, `role`, `content`, `metadata`, `version`, `create_time`, `is_deleted`, `create_by`, `update_by`, `update_time`) VALUES
(71001, 70001, 'user', '请解释默认考勤组的上下班规则。', JSON_OBJECT('module', 'attendance'), 0, NOW(), 0, 1, 1, NOW()),
(71002, 70001, 'assistant', '默认考勤组采用固定班次，工作时间为 09:00 到 18:00，午休 12:00 到 13:30。', JSON_OBJECT('module', 'attendance', 'source', 'seed'), 0, NOW(), 0, 1, 1, NOW()),
(71003, 70002, 'user', '我什么时候可以看到 2026-06 的工资条？', JSON_OBJECT('module', 'salary'), 0, NOW(), 0, 14001, 14001, NOW()),
(71004, 70002, 'assistant', '2026-06 薪资批次已审批完成，普通员工可以查看工资条。', JSON_OBJECT('module', 'salary', 'batchId', 44002), 0, NOW(), 0, 14001, 14001, NOW())
ON DUPLICATE KEY UPDATE `content` = VALUES(`content`), `metadata` = VALUES(`metadata`), `update_by` = VALUES(`update_by`), `update_time` = VALUES(`update_time`), `is_deleted` = VALUES(`is_deleted`);
