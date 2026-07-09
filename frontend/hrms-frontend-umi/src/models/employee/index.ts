/**
 * 员工状态管理
 * 负责人：成员 B
 */

import type { Effect, Reducer } from 'umi';
import { getEmployeeList, getEmployeeDetail } from '@/services/employee';

export interface EmployeeState {
  list: any[];
  current: any | null;
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
}

export interface EmployeeModelType {
  namespace: string;
  state: EmployeeState;
  effects: {
    fetchList: Effect;
    fetchDetail: Effect;
  };
  reducers: {
    setList: Reducer<EmployeeState>;
    setCurrent: Reducer<EmployeeState>;
    resetState: Reducer<EmployeeState>;
  };
}

const EmployeeModel: EmployeeModelType = {
  namespace: 'employee',

  state: {
    list: [],
    current: null,
    pagination: {
      current: 1,
      pageSize: 10,
      total: 0,
    },
  },

  effects: {
    *fetchList({ payload }, { call, put }) {
      const response = yield call(getEmployeeList, payload);
      yield put({
        type: 'setList',
        payload: response,
      });
    },
    *fetchDetail({ payload }, { call, put }) {
      const response = yield call(getEmployeeDetail, payload.id);
      yield put({
        type: 'setCurrent',
        payload: response,
      });
    },
  },

  reducers: {
    setList(state, { payload }) {
      return {
        ...state,
        list: payload?.records || [],
        pagination: {
          ...state.pagination,
          current: payload?.pageNum || 1,
          total: payload?.total || 0,
        },
      };
    },
    setCurrent(state, { payload }) {
      return {
        ...state,
        current: payload,
      };
    },
    resetState() {
      return {
        list: [],
        current: null,
        pagination: {
          current: 1,
          pageSize: 10,
          total: 0,
        },
      };
    },
  },
};

export default EmployeeModel;