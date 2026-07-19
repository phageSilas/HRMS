-- ================================================================
-- 考勤测试数据 — 请在数据库客户端 (Navicat/DataGrip/DBeaver) 中执行
-- 执行前先确认当前 hrms 数据库
-- ================================================================

-- 1) 员工1（系统管理员）7月两周完整打卡数据
INSERT IGNORE INTO hr_attendance_record (employee_id, group_id, record_date, clock_in_time, clock_out_time, clock_in_status, clock_out_status, correction_status) VALUES
-- 第3周：正常出勤
(1, 1, '2026-07-13', '2026-07-13 09:00:00', '2026-07-13 18:00:00', 'NORMAL', 'NORMAL', 'NONE'),
(1, 1, '2026-07-14', '2026-07-14 09:00:00', '2026-07-14 18:00:00', 'NORMAL', 'NORMAL', 'NONE'),
(1, 1, '2026-07-15', '2026-07-15 09:05:00', '2026-07-15 18:00:00', 'NORMAL', 'NORMAL', 'NONE'),
(1, 1, '2026-07-16', '2026-07-16 09:00:00', '2026-07-16 18:00:00', 'NORMAL', 'NORMAL', 'NONE'),
(1, 1, '2026-07-17', '2026-07-17 09:00:00', '2026-07-17 18:00:00', 'NORMAL', 'NORMAL', 'NONE'),
-- 第4周：含迟到和早退
(1, 1, '2026-07-20', '2026-07-20 09:00:00', '2026-07-20 18:00:00', 'NORMAL', 'NORMAL', 'NONE'),
(1, 1, '2026-07-21', '2026-07-21 09:00:00', '2026-07-21 18:00:00', 'NORMAL', 'NORMAL', 'NONE'),
(1, 1, '2026-07-22', '2026-07-22 09:35:00', '2026-07-22 18:00:00', 'LATE', 'NORMAL', 'NONE'),
(1, 1, '2026-07-23', '2026-07-23 09:00:00', '2026-07-23 17:00:00', 'NORMAL', 'EARLY_LEAVE', 'NONE'),
(1, 1, '2026-07-24', '2026-07-24 09:00:00', '2026-07-24 18:00:00', 'NORMAL', 'NORMAL', 'NONE');

-- 2) 员工1 补卡记录（上班卡 + 下班卡）
INSERT IGNORE INTO hr_attendance_correction (employee_id, correction_date, correction_type, correction_reason, approval_status)
VALUES
(1, '2026-07-22', 'CLOCK_IN', '通勤地铁故障导致迟到，申请补卡', 1),  -- 审批中
(1, '2026-07-23', 'CLOCK_OUT', '临时会议忘记打下班卡', 2);         -- 已通过

-- 3) 员工1 加班记录
INSERT IGNORE INTO hr_attendance_overtime (employee_id, overtime_date, duration, reason, approval_status)
VALUES
(1, '2026-07-18 10:00:00', 3.0, '周末系统升级维护', 2),    -- 已通过
(1, '2026-07-19 14:00:00', 2.5, '数据迁移支持', 1),       -- 审批中
(1, '2026-07-25 18:00:00', 2.0, '月末报表整理', 0);       -- 草稿

-- 4) 员工5（孙七）请假记录对应的考勤标记 → 确保日历显示 LEAVE 状态
--    7月23日已通过年假 → 考勤记录标记为 ABSENCE
INSERT IGNORE INTO hr_attendance_record (employee_id, group_id, record_date, clock_in_time, clock_out_time, clock_in_status, clock_out_status, correction_status)
VALUES (5, 1, '2026-07-23', NULL, NULL, 'ABSENCE', 'ABSENCE', 'NONE');

-- 5) 验证：查看员工1数据
SELECT '=== 员工1 考勤记录 ===' AS '';
SELECT record_date, clock_in_status, clock_out_status, correction_status
FROM hr_attendance_record WHERE employee_id = 1 AND record_date >= '2026-07-13' ORDER BY record_date;

SELECT '=== 员工1 补卡记录 ===' AS '';
SELECT * FROM hr_attendance_correction WHERE employee_id = 1;

SELECT '=== 员工1 加班记录 ===' AS '';
SELECT * FROM hr_attendance_overtime WHERE employee_id = 1;

SELECT '=== 员工5 请假日考勤记录 ===' AS '';
SELECT record_date, clock_in_status, clock_out_status
FROM hr_attendance_record WHERE employee_id = 5 AND record_date = '2026-07-23';

SELECT '=== 员工5 请假记录 ===' AS '';
SELECT id, leave_type, start_time, end_time, approval_status FROM hr_leave_request WHERE employee_id = 5 ORDER BY start_time;
