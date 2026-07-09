/**
 * 系统域状态管理
 * 负责人：成员 A
 */

import type { Reducer } from 'umi';

export interface SystemState {
  userList: any[];
  roleList: any[];
  menuList: any[];
  departmentTree: any[];
  postList: any[];
  dictData: Record<string, any[]>;
}

export interface SystemModelType {
  namespace: string;
  state: SystemState;
  effects: {};
  reducers: {
    resetState: Reducer<SystemState>;
  };
}

const SystemModel: SystemModelType = {
  namespace: 'system',

  state: {
    userList: [],
    roleList: [],
    menuList: [],
    departmentTree: [],
    postList: [],
    dictData: {},
  },

  effects: {},

  reducers: {
    resetState() {
      return {
        userList: [],
        roleList: [],
        menuList: [],
        departmentTree: [],
        postList: [],
        dictData: {},
      };
    },
  },
};

export default SystemModel;