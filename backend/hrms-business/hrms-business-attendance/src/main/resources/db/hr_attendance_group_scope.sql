ALTER TABLE hr_attendance_group
  ADD COLUMN scope_type varchar(32) NULL COMMENT '适用范围类型：DEPT/POST/EMPLOYEE' AFTER monthly_correction_limit,
  ADD COLUMN scope_value varchar(1000) NULL COMMENT '适用范围值，JSON字符串' AFTER scope_type;
