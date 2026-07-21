/**
 * 考勤域状态管理
 * 为考勤记录、请假和汇总页面保留统一的历史状态容器。
 * 负责人：成员 C
 */

import type { Reducer } from 'umi';

export interface AttendanceState {
  recordList: any[];
  leaveList: any[];
  summaryList: any[];
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
}

export interface AttendanceModelType {
  namespace: string;
  state: AttendanceState;
  effects: {};
  reducers: {
    resetState: Reducer<AttendanceState>;
  };
}

const AttendanceModel: AttendanceModelType = {
  namespace: 'attendance',

  state: {
    recordList: [],
    leaveList: [],
    summaryList: [],
    pagination: {
      current: 1,
      pageSize: 10,
      total: 0,
    },
  },

  effects: {},

  reducers: {
    /** 重置考勤模块的列表与分页状态，便于页面重新进入时恢复初始值。 */
    resetState() {
      return {
        recordList: [],
        leaveList: [],
        summaryList: [],
        pagination: {
          current: 1,
          pageSize: 10,
          total: 0,
        },
      };
    },
  },
};

export default AttendanceModel;
