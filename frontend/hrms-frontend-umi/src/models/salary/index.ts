/**
 * 薪资域状态管理
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