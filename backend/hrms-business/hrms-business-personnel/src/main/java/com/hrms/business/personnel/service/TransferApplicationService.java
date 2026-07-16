package com.hrms.business.personnel.service;

import com.hrms.business.personnel.dto.TransferApplicationCreateRequestDTO;
import com.hrms.business.personnel.dto.TransferApplicationQueryDTO;
import com.hrms.business.personnel.vo.TransferApplicationCreateVO;
import com.hrms.business.personnel.vo.TransferApplicationPageVO;
import com.hrms.common.web.PageResult;

/**
 * 调岗申请服务接口
 */
public interface TransferApplicationService {

    /**
     * 分页查询调岗申请列表。
     *
     * @param queryDTO 调岗申请查询参数
     * @return 调岗申请分页结果
     * 本方法使用的工具类: 无
     */
    PageResult<TransferApplicationPageVO> pageTransferApplications(TransferApplicationQueryDTO queryDTO);

    /**
     * 创建调岗申请。
     *
     * @param requestDTO 调岗申请创建参数
     * @return 调岗申请创建结果
     * 本方法使用的工具类: 无
     */
    TransferApplicationCreateVO createTransferApplication(TransferApplicationCreateRequestDTO requestDTO);

}
