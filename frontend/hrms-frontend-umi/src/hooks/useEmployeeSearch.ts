/**
 * 员工搜索 Hook
 *
 * 封装 300ms 防抖 + getEmployeeList 的员工搜索模式，
 * 提取自 delegation/index.tsx（行96-122）和 detail/index.tsx（行246-265）的重复逻辑。
 */
import { useCallback, useRef, useState } from 'react';
import { getEmployeeList } from '@/services/employee';
import type { EmployeeBrief } from '@/services/employee';

/** Hook 返回值 */
export interface EmployeeSearchState {
  /** 员工选项列表（用于 Select 组件） */
  options: EmployeeBrief[];
  /** 是否正在搜索 */
  searching: boolean;
  /** 触发搜索（防抖 300ms） */
  search: (keyword: string) => void;
}

/**
 * useEmployeeSearch — 员工搜索（300ms 防抖）
 *
 * @returns { options, searching, search }
 *
 * @example
 * ```tsx
 * const { options, searching, search } = useEmployeeSearch();
 * // 在 Select 组件中使用：
 * <Select
 *   showSearch
 *   filterOption={false}
 *   loading={searching}
 *   onSearch={search}
 *   options={options.map(emp => ({
 *     label: `${emp.employeeName}（${emp.employeeNo}）`,
 *     value: emp.id,
 *   }))}
 * />
 * ```
 */
export function useEmployeeSearch(): EmployeeSearchState {
  const [options, setOptions] = useState<EmployeeBrief[]>([]);
  const [searching, setSearching] = useState(false);
  const searchTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const search = useCallback((keyword: string) => {
    if (searchTimerRef.current) {
      clearTimeout(searchTimerRef.current);
    }

    if (!keyword || keyword.length < 1) {
      setOptions([]);
      return;
    }

    searchTimerRef.current = setTimeout(async () => {
      setSearching(true);
      try {
        const result = await getEmployeeList({ keyword, pageNum: 1, pageSize: 20 });
        setOptions(result.records || []);
      } catch {
        setOptions([]);
      } finally {
        setSearching(false);
      }
    }, 300);
  }, []);

  return { options, searching, search };
}
