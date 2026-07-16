/**
 * 员工状态管理
 * 负责人：成员 B
 */

import {
  getEmployeeList,
  getEmployeeDetail,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  getContractList,
  getFieldPermissions,
  type Employee,
  type EmployeeBrief,
  type EmployeeCreateRequest,
  type EmployeeQuery,
  type Contract,
  type FieldPermissions,
} from '@/services/employee';
import { useState, useCallback } from 'react';

export interface EmployeeListState {
  list: EmployeeBrief[];
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
}

export interface ContractListState {
  list: Contract[];
  pagination: {
    current: number;
    pageSize: number;
    total: number;
  };
}

export default function useEmployeeModel() {
  // 员工列表
  const [employeeList, setEmployeeList] = useState<EmployeeListState>({
    list: [],
    pagination: { current: 1, pageSize: 20, total: 0 },
  });
  const [listLoading, setListLoading] = useState(false);

  // 当前员工详情
  const [currentEmployee, setCurrentEmployee] = useState<Employee | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // 字段权限
  const [fieldPermissions, setFieldPermissions] =
    useState<FieldPermissions | null>(null);

  // 合同列表
  const [contractList, setContractList] = useState<ContractListState>({
    list: [],
    pagination: { current: 1, pageSize: 20, total: 0 },
  });
  const [contractLoading, setContractLoading] = useState(false);

  // 提交中
  const [submitting, setSubmitting] = useState(false);

  /** 获取员工列表 */
  const fetchList = useCallback(async (params: EmployeeQuery = {}) => {
    setListLoading(true);
    try {
      const data = await getEmployeeList({
        pageNum: 1,
        pageSize: 20,
        ...params,
      });
      if (data) {
        setEmployeeList({
          list: data.records || [],
          pagination: {
            current: data.pageNum || 1,
            pageSize: data.pageSize || 20,
            total: data.total || 0,
          },
        });
      }
    } finally {
      setListLoading(false);
    }
  }, []);

  /** 获取员工详情 */
  const fetchDetail = useCallback(async (id: number) => {
    setDetailLoading(true);
    try {
      const data = await getEmployeeDetail(id);
      if (data) {
        setCurrentEmployee(data);
      }
      return data;
    } finally {
      setDetailLoading(false);
    }
  }, []);

  /** 创建员工 */
  const create = useCallback(async (data: EmployeeCreateRequest) => {
    setSubmitting(true);
    try {
      const result = await createEmployee(data);
      return result;
    } finally {
      setSubmitting(false);
    }
  }, []);

  /** 更新员工 */
  const update = useCallback(
    async (id: number, data: Partial<EmployeeCreateRequest>) => {
      setSubmitting(true);
      try {
        const result = await updateEmployee(id, data);
        return result;
      } finally {
        setSubmitting(false);
      }
    },
    [],
  );

  /** 删除员工 */
  const remove = useCallback(async (id: number) => {
    setSubmitting(true);
    try {
      await deleteEmployee(id);
    } finally {
      setSubmitting(false);
    }
  }, []);

  /** 获取字段权限 */
  const fetchFieldPermissions = useCallback(async () => {
    try {
      const data = await getFieldPermissions();
      if (data) {
        setFieldPermissions(data);
      }
      return data;
    } catch {
      return null;
    }
  }, []);

  /** 获取合同列表 */
  const fetchContractList = useCallback(
    async (params: {
      keyword?: string;
      contractStatus?: string;
      pageNum?: number;
      pageSize?: number;
    } = {}) => {
      setContractLoading(true);
      try {
        const data = await getContractList({
          pageNum: 1,
          pageSize: 20,
          ...params,
        });
        if (data) {
          setContractList({
            list: data.records || [],
            pagination: {
              current: data.pageNum || 1,
              pageSize: data.pageSize || 20,
              total: data.total || 0,
            },
          });
        }
      } finally {
        setContractLoading(false);
      }
    },
    [],
  );

  return {
    // 员工列表
    employeeList,
    listLoading,
    fetchList,
    // 员工详情
    currentEmployee,
    detailLoading,
    fetchDetail,
    // 增删改
    create,
    update,
    remove,
    submitting,
    // 字段权限
    fieldPermissions,
    fetchFieldPermissions,
    // 合同
    contractList,
    contractLoading,
    fetchContractList,
  };
}
