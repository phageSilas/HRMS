package com.hrms.business.personnel.service;

import com.hrms.business.personnel.vo.EntryApplicationPageVO;
import com.hrms.business.personnel.vo.LeaveApplicationPageVO;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;
import com.hrms.business.personnel.vo.TransferApplicationPageVO;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.DeptDetailVO;
import com.hrms.system.organization.vo.PostVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 入转调离列表展示字段补齐测试
 */
@ExtendWith(MockitoExtension.class)
class PersonnelDisplayEnricherTest {

    @Mock
    private DeptService deptService;

    @Mock
    private PostService postService;

    private PersonnelDisplayEnricher personnelDisplayEnricher;

    @BeforeEach
    void setUp() {
        personnelDisplayEnricher = new PersonnelDisplayEnricher(deptService, postService);
    }

    /**
     * 验证入职申请会补齐部门和职位名称，并复用请求内缓存避免重复查询。
     *
     * 本方法使用的工具类: DeptService(hrms-system-organization),PostService(hrms-system-organization)
     */
    @Test
    void shouldEnrichEntryApplicationNamesAndReuseCache() {
        DeptDetailVO deptDetail = new DeptDetailVO();
        deptDetail.setDeptName("技术部");
        PostVO postVO = new PostVO();
        postVO.setPostName("Java 开发工程师");
        when(deptService.getDeptById(2L)).thenReturn(deptDetail);
        when(postService.getPostById(102L)).thenReturn(postVO);

        EntryApplicationPageVO first = EntryApplicationPageVO.builder()
                .deptId(2L)
                .postId(102L)
                .build();
        EntryApplicationPageVO second = EntryApplicationPageVO.builder()
                .deptId(2L)
                .postId(102L)
                .build();

        personnelDisplayEnricher.enrichEntryApplication(first);
        personnelDisplayEnricher.enrichEntryApplication(second);

        assertEquals("技术部", first.getDeptName());
        assertEquals("Java 开发工程师", first.getPostName());
        assertEquals("技术部", second.getDeptName());
        assertEquals("Java 开发工程师", second.getPostName());
        verify(deptService, times(1)).getDeptById(2L);
        verify(postService, times(1)).getPostById(102L);
    }

    /**
     * 验证转正、调岗、离职页面 VO 会补齐真实名称，并在名称不存在时保留空值。
     *
     * 本方法使用的工具类: DeptService(hrms-system-organization),PostService(hrms-system-organization)
     */
    @Test
    void shouldEnrichRegularTransferAndLeaveVoNames() {
        DeptDetailVO productDept = new DeptDetailVO();
        productDept.setDeptName("产品部");
        PostVO productPost = new PostVO();
        productPost.setPostName("产品经理");
        when(deptService.getDeptById(3L)).thenReturn(productDept);
        when(postService.getPostById(104L)).thenReturn(productPost);
        when(deptService.getDeptById(999L)).thenReturn(null);

        RegularApplicationPageVO regular = RegularApplicationPageVO.builder()
                .deptId(3L)
                .postId(104L)
                .build();
        TransferApplicationPageVO transfer = TransferApplicationPageVO.builder()
                .fromDeptName(null)
                .fromPostName(null)
                .toDeptName(null)
                .toPostName(null)
                .build();
        LeaveApplicationPageVO leave = LeaveApplicationPageVO.builder().build();

        personnelDisplayEnricher.enrichRegularApplication(regular);
        personnelDisplayEnricher.enrichTransferApplication(transfer, 3L, 104L, 999L, null);
        personnelDisplayEnricher.enrichLeaveApplication(leave, 999L);

        assertEquals("产品部", regular.getDepartmentName());
        assertEquals("产品经理", regular.getPositionName());
        assertEquals("产品部", transfer.getFromDeptName());
        assertEquals("产品经理", transfer.getFromPostName());
        assertNull(transfer.getToDeptName());
        assertNull(transfer.getToPostName());
        assertNull(leave.getDepartmentName());
    }
}
