-- HRMS 服务器统一 SQL 执行入口
SOURCE ./00_初始化数据库.sql;
SOURCE ./权限auth/建表/01_权限认证建表.sql;
SOURCE ./组织架构organization/建表/01_组织架构建表.sql;
SOURCE ./员工employee/建表/01_员工档案建表.sql;
SOURCE ./审批approval/建表/01_审批建表.sql;
SOURCE ./入转调离personnel/建表/01_入转调离建表.sql;
SOURCE ./考勤attendance/建表/01_考勤建表.sql;
SOURCE ./薪资salary/建表/01_薪资建表.sql;
SOURCE ./ai/建表/01_AI建表.sql;
SOURCE ./基础common/建表/01_公共基础建表.sql;

SOURCE ./组织架构organization/初始数据/01_组织架构初始数据.sql;
SOURCE ./权限auth/初始数据/01_权限认证初始数据.sql;
SOURCE ./员工employee/初始数据/01_员工档案初始数据.sql;
SOURCE ./入转调离personnel/初始数据/01_入转调离初始数据.sql;
SOURCE ./考勤attendance/初始数据/01_考勤初始数据.sql;
SOURCE ./薪资salary/初始数据/01_薪资初始数据.sql;
SOURCE ./审批approval/初始数据/01_审批初始数据.sql;
SOURCE ./ai/初始数据/01_AI初始数据.sql;
SOURCE ./基础common/初始数据/01_公共基础初始数据.sql;

SET FOREIGN_KEY_CHECKS = 1;
