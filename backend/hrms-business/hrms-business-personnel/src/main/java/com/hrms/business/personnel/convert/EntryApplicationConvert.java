package com.hrms.business.personnel.convert;

import com.hrms.business.personnel.entity.EntryApplicationEntity;
import com.hrms.business.personnel.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.dto.EntryApplicationCreateOrUpdateRequestDTO;
import com.hrms.business.personnel.vo.EntryApplicationPageVO;

import java.math.BigDecimal;

/**
 * 入职申请转换器
 */
public final class EntryApplicationConvert {

    private static final BigDecimal DEFAULT_PROBATION_SALARY_RATIO = new BigDecimal("80.00");

    private EntryApplicationConvert() {
    }

    /**
     * 将入职申请请求 DTO 转换为实体。
     *
     * @param requestDTO 入职申请请求
     * @return 入职申请实体
     */
    public static EntryApplicationEntity toEntity(EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        EntryApplicationEntity entity = new EntryApplicationEntity();
        fillEntity(entity, requestDTO);
        return entity;
    }

    /**
     * 使用请求参数填充入职申请实体。
     *
     * @param entity 入职申请实体
     * @param requestDTO 入职申请请求
     */
    public static void fillEntity(EntryApplicationEntity entity, EntryApplicationCreateOrUpdateRequestDTO requestDTO) {
        entity.setCandidateName(requestDTO.getCandidateName());
        entity.setGender(requestDTO.getGender());
        entity.setPhone(requestDTO.getPhone());
        entity.setEmail(requestDTO.getEmail());
        entity.setIdCardNo(requestDTO.getIdCardNo());
        entity.setDeptId(requestDTO.getDeptId());
        entity.setPostId(requestDTO.getPostId());
        entity.setHireType(requestDTO.getHireType());
        entity.setProbationMonth(requestDTO.getProbationMonth());
        entity.setProbationSalaryRatio(requestDTO.getProbationSalaryRatio() == null
                ? DEFAULT_PROBATION_SALARY_RATIO
                : requestDTO.getProbationSalaryRatio());
        entity.setExpectedHireDate(requestDTO.getExpectedHireDate());
        entity.setLeaderId(requestDTO.getLeaderId());
    }

    /**
     * 将入职申请实体转换为分页 VO。
     *
     * @param entity 入职申请实体
     * @return 入职申请分页 VO
     */
    public static EntryApplicationPageVO toPageVO(EntryApplicationEntity entity) {
        return EntryApplicationPageVO.builder()
                .id(entity.getId())
                .candidateName(entity.getCandidateName())
                .gender(entity.getGender())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .deptId(entity.getDeptId())
                .deptName(tempResolveDeptName(entity.getDeptId()))
                .postId(entity.getPostId())
                .postName(tempResolvePostName(entity.getPostId()))
                .expectedHireDate(entity.getExpectedHireDate())
                .approvalStatus(entity.getApprovalStatus())
                .approvalStatusDesc(ApplicationStatusEnum.getDescByCode(entity.getApprovalStatus()))
                .approvalInstanceId(entity.getApprovalInstanceId())
                .createTime(entity.getCreateTime())
                .build();
    }

    /**
     * 临时解析部门名称。
     *
     * @param deptId 部门ID
     * @return 部门名称
     */
    private static String tempResolveDeptName(Long deptId) {
        return null;
    }

    /**
     * 临时解析岗位名称。
     *
     * @param postId 岗位ID
     * @return 岗位名称
     */
    private static String tempResolvePostName(Long postId) {
        return null;
    }

}
