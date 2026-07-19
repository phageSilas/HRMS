package com.hrms.business.mycenter.service.impl;

import com.hrms.business.mycenter.dto.ProfileUpdateRequest;
import com.hrms.business.mycenter.dto.ProfileVO;
import com.hrms.business.mycenter.mapper.ProfileMapper;
import com.hrms.business.mycenter.service.ProfileService;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.system.auth.entity.UserEntity;
import com.hrms.system.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * 个人档案服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileMapper profileMapper;
    private final UserMapper userMapper;

    @Override
    public ProfileVO getProfile(Long userId) {
        // 方式一：通过 hr_employee.user_id 查询（反向关联，最可靠）
        ProfileVO profile = profileMapper.selectProfileByUserId(userId);
        if (profile == null) {
            // 方式二：通过 sys_user.employee_id（正向关联）查 hr_employee.id
            UserEntity user = userMapper.selectById(userId);
            if (user != null && user.getEmployeeId() != null) {
                profile = profileMapper.selectProfileByEmployeeId(user.getEmployeeId());
            }
        }
        if (profile == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "员工信息不存在");
        }

        // 性别描述
        if (profile.getGender() != null) {
            switch (profile.getGender()) {
                case 1 -> profile.setGenderDesc("男");
                case 2 -> profile.setGenderDesc("女");
                default -> profile.setGenderDesc("未知");
            }
        }

        // 在职状态描述（与 EmployeeEntity.employmentStatus 一致）
        if (profile.getEmploymentStatus() != null) {
            switch (profile.getEmploymentStatus()) {
                case 1 -> profile.setEmploymentStatusDesc("试用期");
                case 2 -> profile.setEmploymentStatusDesc("正式");
                case 3 -> profile.setEmploymentStatusDesc("待离职");
                case 4 -> profile.setEmploymentStatusDesc("已离职");
                default -> profile.setEmploymentStatusDesc("未知");
            }
        }

        // 字段权限（静态配置，后续可对接 FieldPermissionService）
        ProfileVO.FieldPermissions fp = new ProfileVO.FieldPermissions();
        // 联系信息可编辑
        fp.setEditableFields(Arrays.asList("email", "currentAddress", "emergencyContact", "emergencyPhone"));
        // 需走流程（联系 HR）的字段
        fp.setFlowRequiredFields(Arrays.asList("phone", "deptName", "postName", "employeeNo", "hireDate"));
        // 完全锁定的字段
        fp.setLockedFields(Arrays.asList("idCard", "bankAccount"));
        profile.setFieldPermissions(fp);

        return profile;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        // 通过 userId 找到 employeeId
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getEmployeeId() == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "用户关联的员工信息不存在");
        }

        int affected = profileMapper.updateProfile(
                user.getEmployeeId(),
                request.getEmail(),
                request.getPhone(),
                request.getCurrentAddress(),
                request.getEmergencyContact(),
                request.getEmergencyPhone()
        );
        if (affected <= 0) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "员工信息不存在");
        }
    }
}
