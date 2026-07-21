/**
 * 薪资域状态管理
 * 为账套、薪资批次和工资条页面保留统一的历史状态容器。
 * 负责人：成员 C
 */

import type { Reducer } from 'umi';

export interface SalaryState {
  accountList: any[];
  batchList: any[];
  payslipList: any[];
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
}

export interface SalaryModelType {
  namespace: string;
  state: SalaryState;
  effects: {};
  reducers: {
    resetState: Reducer<SalaryState>;
  };
}

const SalaryModel: SalaryModelType = {
  namespace: 'salary',

  state: {
    accountList: [],
    batchList: [],
    payslipList: [],
    pagination: {
      current: 1,
      pageSize: 10,
      total: 0,
    },
  },

  effects: {},

  reducers: {
    /** 重置薪资模块的列表与分页状态，便于页面重新进入时恢复默认值。 */
    resetState() {
      return {
        accountList: [],
        batchList: [],
        payslipList: [],
        pagination: {
          current: 1,
          pageSize: 10,
          total: 0,
        },
      };
    },
  },
};

export default SalaryModel;
