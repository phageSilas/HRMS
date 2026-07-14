package com.hrms.business.personnel.service;

import com.hrms.business.personnel.dto.RegularApplicationQueryDTO;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;
import com.hrms.common.web.PageResult;

/**
 * 转正申请服务接口
 */
public interface RegularApplicationService {

    /**
     * 分页查询转正申请列表。
     *
     * @param queryDTO 转正申请查询参数
     * @return 转正申请分页结果
     * 本方法使用的工具类: 无
     */
    PageResult<RegularApplicationPageVO> pageRegularApplications(RegularApplicationQueryDTO queryDTO);

}
