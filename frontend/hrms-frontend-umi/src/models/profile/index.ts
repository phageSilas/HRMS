/**
 * 个人中心状态管理
 * 负责人：成员 D
 */

import type { Reducer } from 'umi';

export interface ProfileState {
  myProfile: any | null;
  myAttendance: any[];
  mySalary: any[];
  myApplications: any[];
}

export interface ProfileModelType {
  namespace: string;
  state: ProfileState;
  effects: {};
  reducers: {
    resetState: Reducer<ProfileState>;
  };
}

const ProfileModel: ProfileModelType = {
  namespace: 'profile',

  state: {
    myProfile: null,
    myAttendance: [],
    mySalary: [],
    myApplications: [],
  },

  effects: {},

  reducers: {
    resetState() {
      return {
        myProfile: null,
        myAttendance: [],
        mySalary: [],
        myApplications: [],
      };
    },
  },
};

export default ProfileModel;