/**
 * 考勤域状态管理
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