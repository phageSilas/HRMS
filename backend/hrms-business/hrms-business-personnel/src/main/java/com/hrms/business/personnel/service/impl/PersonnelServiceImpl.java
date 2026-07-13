package com.hrms.business.personnel.service.impl;

import com.hrms.business.personnel.service.PersonnelService;
import org.springframework.stereotype.Service;

/**
 * 入转调离服务实现
 */
@Service
public class PersonnelServiceImpl implements PersonnelService {

    @Override
    public Long startEntryApproval(Long employeeId) {
        // TODO: 实现入职审批逻辑
        return null;
    }

    @Override
    public Long startRegularApproval(Long employeeId) {
        // TODO: 实现转正审批逻辑
        return null;
    }

    @Override
    public Long startTransferApproval(Long employeeId, Long newDeptId, Long newPostId) {
        // TODO: 实现调岗审批逻辑
        return null;
    }

    @Override
    public Long startResignApproval(Long employeeId) {
        // TODO: 实现离职审批逻辑
        return null;
    }

}
