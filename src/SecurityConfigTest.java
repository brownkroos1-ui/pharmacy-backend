package com.pharmacy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.TestControllers.class)
@Import(SecurityConfig.class)
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // SecurityConfig has these dependencies, so we must mock them in a @WebMvcTest
    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    // Define dummy controllers to test the security rules against.
    @RestController
    static class TestControllers {
        @GetMapping("/api/admin/test")
        public String adminEndpoint() { return "Admin Content"; }

        @GetMapping("/api/user/test")
        public String userEndpoint() { return "User Content"; }

        @PostMapping({"/api/auth/register", "/auth/register"})
        public String registerEndpoint() { return "Registered"; }
    }

    @Test
    void testAccessToAdminEndpoint_WithoutAuth_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAccessToAdminEndpoint_WithAdminRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAccessToAdminEndpoint_WithUserRole_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAccessToUserEndpoint_WithUserRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/user/test"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAccessToUserEndpoint_WithAdminRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/user/test"))
                .andExpect(status().isOk());

        // Admin should also be able to access admin
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isOk());
    }

    @Test
    void testRegistrationEndpoint_IsPubliclyAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/register"))
                .andExpect(status().isOk());

        // Also allow /auth/register (without /api prefix)
        mockMvc.perform(post("/auth/register"))
                .andExpect(status().isOk());
    }
}
