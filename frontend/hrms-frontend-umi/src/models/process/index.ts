/**
 * 流程状态管理
 * 负责人：成员 B
 */

import type { Effect, Reducer } from 'umi';

export interface ProcessState {
  entryList: any[];
  regularList: any[];
  transferList: any[];
  leaveList: any[];
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
}

export interface ProcessModelType {
  namespace: string;
  state: ProcessState;
  effects: {};
  reducers: {
    resetState: Reducer<ProcessState>;
  };
}

const ProcessModel: ProcessModelType = {
  namespace: 'process',

  state: {
    entryList: [],
    regularList: [],
    transferList: [],
    leaveList: [],
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
        entryList: [],
        regularList: [],
        transferList: [],
        leaveList: [],
        pagination: {
          current: 1,
          pageSize: 10,
          total: 0,
        },
      };
    },
  },
};

export default ProcessModel;