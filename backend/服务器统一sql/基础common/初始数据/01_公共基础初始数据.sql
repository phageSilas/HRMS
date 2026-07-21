-- 公共基础模块初始数据
USE `hrms`;

INSERT INTO `sys_file` (`id`, `file_name`, `file_path`, `file_size`, `file_type`, `mime_type`, `md5`, `business_type`, `business_id`, `create_by`, `update_by`, `is_deleted`) VALUES
(80001, 'leave-proof-14002.jpg', '/mock/leave/leave-proof-14002.jpg', 24576, 'jpg', 'image/jpeg', 'mock-md5-14002', 'leave_request', 73012, 14002, 14002, 0)
ON DUPLICATE KEY UPDATE `file_path` = VALUES(`file_path`), `business_type` = VALUES(`business_type`), `business_id` = VALUES(`business_id`), `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_operate_log` (`id`, `user_id`, `username`, `operate_type`, `operate_module`, `operate_desc`, `request_method`, `request_url`, `request_params`, `response_result`, `ip`, `user_agent`, `execute_time`, `status`, `error_msg`, `create_time`) VALUES
(81001, 1, 'admin', 'IMPORT', 'sql-init', '执行服务器统一 SQL 初始化', 'SOURCE', '/docs/数据库/服务器统一sql/00_执行全部.sql', '{"env":"local"}', '{"code":20000,"message":"success"}', '127.0.0.1', 'mysql-client', 2150, 1, NULL, NOW())
ON DUPLICATE KEY UPDATE `operate_desc` = VALUES(`operate_desc`), `response_result` = VALUES(`response_result`), `execute_time` = VALUES(`execute_time`), `status` = VALUES(`status`), `create_time` = VALUES(`create_time`);

INSERT INTO `sys_login_log` (`id`, `user_id`, `username`, `login_type`, `ip`, `login_location`, `browser`, `os`, `status`, `error_msg`, `login_time`) VALUES
(82001, 1, 'admin', 'ACCOUNT', '127.0.0.1', '本机', 'Chrome', 'Windows 11', 1, NULL, NOW())
ON DUPLICATE KEY UPDATE `login_type` = VALUES(`login_type`), `status` = VALUES(`status`), `login_time` = VALUES(`login_time`);
