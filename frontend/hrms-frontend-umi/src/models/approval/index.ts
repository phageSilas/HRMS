/**
 * 审批域状态管理
 * 负责人：成员 D
 */

import type { Reducer } from 'umi';

export interface ApprovalState {
  pendingList: any[];
  doneList: any[];
  currentInstance: any | null;
  pendingCount: number;
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
}

export interface ApprovalModelType {
  namespace: string;
  state: ApprovalState;
  effects: {};
  reducers: {
    resetState: Reducer<ApprovalState>;
  };
}

const ApprovalModel: ApprovalModelType = {
  namespace: 'approval',

  state: {
    pendingList: [],
    doneList: [],
    currentInstance: null,
    pendingCount: 0,
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
        pendingList: [],
        doneList: [],
        currentInstance: null,
        pendingCount: 0,
        pagination: {
          current: 1,
          pageSize: 10,
          total: 0,
        },
      };
    },
  },
};

export default ApprovalModel;