/**
 * 审批域状态管理
 *
 * @deprecated 审批中心前端已改用 Hooks + Services 模式（pages/approval/workspace 等），
 *             不再依赖 Dva model。保留此文件仅用于兼容其他模块（如首页）的历史引用。
 *             新代码请直接调用 @/services/approval 中的 API 方法。
 *
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