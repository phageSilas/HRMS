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
        EntryApplicationPageVO vo = new EntryApplicationPageVO();
        vo.setId(entity.getId());
        vo.setCandidateName(entity.getCandidateName());
        vo.setGender(entity.getGender());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setDeptId(entity.getDeptId());
        // orgService.getDeptName(entity.getDeptId()); 本接口需要调用 hrms-system-organization 模块的部门详情接口获取部门名称
        vo.setDeptName(tempResolveDeptName(entity.getDeptId()));
        vo.setPostId(entity.getPostId());
        // orgService.getPostName(entity.getPostId()); 本接口需要调用 hrms-system-organization 模块的岗位详情接口获取岗位名称
        vo.setPostName(tempResolvePostName(entity.getPostId()));
        vo.setExpectedHireDate(entity.getExpectedHireDate());
        vo.setApprovalStatus(entity.getApprovalStatus());
        vo.setApprovalStatusDesc(ApplicationStatusEnum.getDescByCode(entity.getApprovalStatus()));
        vo.setApprovalInstanceId(entity.getApprovalInstanceId());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
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
