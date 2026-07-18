package com.hrms.business.personnel.service;

import com.hrms.business.personnel.vo.EntryApplicationPageVO;
import com.hrms.business.personnel.vo.LeaveApplicationPageVO;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;
import com.hrms.business.personnel.vo.TransferApplicationPageVO;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.DeptDetailVO;
import com.hrms.system.organization.vo.PostVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 入转调离列表展示字段补齐器。
 */
@Slf4j
@RequiredArgsConstructor
public class PersonnelDisplayEnricher {

    private final DeptService deptService;

    private final PostService postService;

    private final Map<Long, String> deptNameCache = new HashMap<>();

    private final Map<Long, String> postNameCache = new HashMap<>();

    /**
     * 补齐入职申请列表展示名称。
     *
     * @param vo 入职申请分页 VO
     * @return 补齐后的 VO
     * 本方法使用的工具类: DeptService(hrms-system-organization),PostService(hrms-system-organization)
     */
    public EntryApplicationPageVO enrichEntryApplication(EntryApplicationPageVO vo) {
        if (vo == null) {
            return null;
        }
        vo.setDeptName(resolveDeptName(vo.getDeptId()));
        vo.setPostName(resolvePostName(vo.getPostId()));
        return vo;
    }

    /**
     * 补齐转正申请列表展示名称。
     *
     * @param vo 转正申请分页 VO
     * @return 补齐后的 VO
     * 本方法使用的工具类: DeptService(hrms-system-organization),PostService(hrms-system-organization)
     */
    public RegularApplicationPageVO enrichRegularApplication(RegularApplicationPageVO vo) {
        if (vo == null) {
            return null;
        }
        vo.setDepartmentName(resolveDeptName(vo.getDeptId()));
        vo.setPositionName(resolvePostName(vo.getPostId()));
        return vo;
    }

    /**
     * 补齐调岗申请列表展示名称。
     *
     * @param vo         调岗申请分页 VO
     * @param fromDeptId 原部门 ID
     * @param fromPostId 原职位 ID
     * @param toDeptId   新部门 ID
     * @param toPostId   新职位 ID
     * @return 补齐后的 VO
     * 本方法使用的工具类: DeptService(hrms-system-organization),PostService(hrms-system-organization)
     */
    public TransferApplicationPageVO enrichTransferApplication(TransferApplicationPageVO vo,
                                                               Long fromDeptId,
                                                               Long fromPostId,
                                                               Long toDeptId,
                                                               Long toPostId) {
        if (vo == null) {
            return null;
        }
        vo.setFromDeptName(resolveDeptName(fromDeptId));
        vo.setFromPostName(resolvePostName(fromPostId));
        vo.setToDeptName(resolveDeptName(toDeptId));
        vo.setToPostName(resolvePostName(toPostId));
        return vo;
    }

    /**
     * 补齐离职申请列表展示名称。
     *
     * @param vo     离职申请分页 VO
     * @param deptId 部门 ID
     * @return 补齐后的 VO
     * 本方法使用的工具类: DeptService(hrms-system-organization)
     */
    public LeaveApplicationPageVO enrichLeaveApplication(LeaveApplicationPageVO vo, Long deptId) {
        if (vo == null) {
            return null;
        }
        vo.setDepartmentName(resolveDeptName(deptId));
        return vo;
    }

    /**
     * 解析部门名称并复用当前请求缓存。
     *
     * @param deptId 部门 ID
     * @return 部门名称
     * 本方法使用的工具类: DeptService(hrms-system-organization),HashMap(JDK)
     */
    private String resolveDeptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        if (deptNameCache.containsKey(deptId)) {
            return deptNameCache.get(deptId);
        }
        try {
            DeptDetailVO deptDetail = deptService.getDeptById(deptId);
            String deptName = deptDetail == null ? null : deptDetail.getDeptName();
            deptNameCache.put(deptId, deptName);
            return deptName;
        } catch (Exception ex) {
            log.warn("resolve personnel dept name failed, deptId={}", deptId, ex);
            deptNameCache.put(deptId, null);
            return null;
        }
    }

    /**
     * 解析职位名称并复用当前请求缓存。
     *
     * @param postId 职位 ID
     * @return 职位名称
     * 本方法使用的工具类: PostService(hrms-system-organization),HashMap(JDK)
     */
    private String resolvePostName(Long postId) {
        if (postId == null) {
            return null;
        }
        if (postNameCache.containsKey(postId)) {
            return postNameCache.get(postId);
        }
        try {
            PostVO postDetail = postService.getPostById(postId);
            String postName = postDetail == null ? null : postDetail.getPostName();
            postNameCache.put(postId, postName);
            return postName;
        } catch (Exception ex) {
            log.warn("resolve personnel post name failed, postId={}", postId, ex);
            postNameCache.put(postId, null);
            return null;
        }
    }
}
