package com.hrms.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证 HRMS 服务端模块的基础装配结果。
 */
@SpringBootTest
@AutoConfigureMockMvc
class HrmsServerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 验证 Spring 容器可以正常启动。
     */
    @Test
    void contextLoads() {
    }

    /**
     * 验证系统基础域接口可以正常访问。
     *
     * @throws Exception 接口调用异常
     */
    @Test
    void shouldReturnSystemSummary() throws Exception {
        mockMvc.perform(get("/api/system/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0000"))
            .andExpect(jsonPath("$.data").value("system module: permission, organization, employee archive"));
    }

    /**
     * 验证业务域接口可以正常访问且依赖系统域服务。
     *
     * @throws Exception 接口调用异常
     */
    @Test
    void shouldReturnBusinessSummary() throws Exception {
        mockMvc.perform(get("/api/business/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0000"))
            .andExpect(jsonPath("$.data").value(
                "business module: onboarding, attendance, payroll, approval, profile; depends on "
                    + "system module: permission, organization, employee archive"
            ));
    }

    /**
     * 验证部门树接口可以正常访问。
     *
     * @throws Exception 接口调用异常
     */
    @Test
    void shouldReturnDepartmentTree() throws Exception {
        mockMvc.perform(get("/api/system/departments/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0000"))
            .andExpect(jsonPath("$.data[0].id").value(1));
    }

    /**
     * 验证员工简要信息接口可以正常访问。
     *
     * @throws Exception 接口调用异常
     */
    @Test
    void shouldReturnEmployeeBrief() throws Exception {
        mockMvc.perform(get("/api/business/employees/brief/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0000"))
            .andExpect(jsonPath("$.data.employeeNo").value("202601001"));
    }
}
