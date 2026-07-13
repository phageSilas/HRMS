package com.hrms.business.personnel.service;

import com.hrms.business.personnel.dto.EntryApplicationQueryDTO;
import com.hrms.business.personnel.vo.EntryApplicationPageVO;
import com.hrms.common.web.PageResult;

/**
 * 入职申请服务接口
 */
public interface EntryApplicationService {

    /**
     * 分页查询入职申请列表。
     *
     * @param queryDTO 入职申请查询参数
     * @return 入职申请分页结果
     */
    PageResult<EntryApplicationPageVO> pageEntryApplications(EntryApplicationQueryDTO queryDTO);

}
