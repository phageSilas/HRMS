/**
 * 首页统计 Mock 数据
 */

import { Request, Response } from 'express';

export default {
  // 员工总数
  'GET /employees/count': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: { count: 128 },
    });
  },

  // 本月入职人数
  'GET /employees/count/month-entry': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: { count: 5 },
    });
  },

  // 待审批数量
  'GET /approval/pending-count': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: {
        count: 8,
        details: [
          { bizType: '入职审批', count: 3 },
          { bizType: '转正审批', count: 2 },
          { bizType: '请假审批', count: 2 },
          { bizType: '调岗审批', count: 1 },
        ],
      },
    });
  },

  // 本月薪资总额
  'GET /salary/monthly-total': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: { total: 1568000.00 },
    });
  },

  // 待审核薪资批次
  'GET /salary/pending-batch-count': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: { count: 2 },
    });
  },

  // 我的考勤汇总
  'GET /my/attendance/summary': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: {
        workDays: 22,
        leaveDays: 1,
        overtimeHours: 4,
        lateTimes: 0,
        earlyLeaveTimes: 0,
      },
    });
  },

  // 我的年假余额
  'GET /my/leave-balance': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: {
        annualLeave: 8,
        sickLeave: 10,
        personalLeave: 5,
      },
    });
  },

  // 我的申请列表
  'GET /my/applications': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: {
        records: [
          {
            id: 1,
            type: '请假申请',
            submitTime: '2024-01-15 10:30:00',
            status: 'APPROVING',
            statusText: '审批中',
            currentStep: '部门主管审批',
          },
          {
            id: 2,
            type: '加班申请',
            submitTime: '2024-01-14 16:00:00',
            status: 'APPROVED',
            statusText: '已通过',
            currentStep: '完成',
          },
          {
            id: 3,
            type: '出差申请',
            submitTime: '2024-01-10 09:00:00',
            status: 'REJECTED',
            statusText: '已拒绝',
            currentStep: '完成',
          },
        ],
        total: 3,
      },
    });
  },

  // 待办任务列表
  'GET /approval/pending-list': (req: Request, res: Response) => {
    res.json({
      code: 0,
      message: 'success',
      data: {
        records: [
          {
            id: 101,
            bizType: '入职审批',
            applicant: '张三',
            deptName: '技术部',
            submitTime: '2024-01-15 09:00:00',
            title: '张三的入职申请',
          },
          {
            id: 102,
            bizType: '转正审批',
            applicant: '李四',
            deptName: '产品部',
            submitTime: '2024-01-14 14:30:00',
            title: '李四的转正申请',
          },
          {
            id: 103,
            bizType: '请假审批',
            applicant: '王五',
            deptName: '运营部',
            submitTime: '2024-01-14 11:00:00',
            title: '王五的请假申请',
          },
          {
            id: 104,
            bizType: '调岗审批',
            applicant: '赵六',
            deptName: '市场部',
            submitTime: '2024-01-13 16:00:00',
            title: '赵六的调岗申请',
          },
          {
            id: 105,
            bizType: '离职审批',
            applicant: '钱七',
            deptName: '财务部',
            submitTime: '2024-01-12 10:00:00',
            title: '钱七的离职申请',
          },
        ],
        total: 8,
      },
    });
  },
};