package com.pharmacy.controller;

import com.pharmacy.dto.AdminDashboardDto;
import com.pharmacy.service.AdminDashboardService;
import com.pharmacy.security.JwtUtil;
import com.pharmacy.security.SecurityConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDashboardController.class)
@Import(SecurityConfig.class)
public class AdminDashboardSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AdminDashboardService dashboardService;

    @MockBean
    private com.pharmacy.security.CustomUserDetailsService userDetailsService;

    @Test
    void adminEndpoint_requiresAdminRole_whenUnauthenticated_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_allowsAdminRole_butForbidsUserRole() throws Exception {
        // admin token
        Claims adminClaims = Jwts.claims();
        adminClaims.setSubject("admin");
        adminClaims.put("role", "ADMIN");

        @SuppressWarnings("unchecked")
        Jws<Claims> adminJws = mock(Jws.class);
        when(adminJws.getBody()).thenReturn(adminClaims);
        when(jwtUtil.parseToken("admin-token")).thenReturn(adminJws);

        // mock loading admin user details
        org.springframework.security.core.userdetails.User adminDetails =
                new org.springframework.security.core.userdetails.User(
                        "admin", "pass",
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(adminDetails);

        AdminDashboardDto dto = new AdminDashboardDto(1, 2, 3, 4, 5, 6.0, 7, 8);
        when(dashboardService.getDashboardCounts()).thenReturn(dto);

        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(1)))
                .andExpect(jsonPath("$.totalMedicines", is(2)))
                .andExpect(jsonPath("$.lowStockMedicines", is(3)))
                .andExpect(jsonPath("$.outOfStockMedicines", is(4)))
                .andExpect(jsonPath("$.totalSales", is(5)))
                .andExpect(jsonPath("$.todaySalesAmount", is(6.0)))
                .andExpect(jsonPath("$.completedSales", is(7)))
                .andExpect(jsonPath("$.cancelledSales", is(8)));

        // cashier token
        Claims userClaims = Jwts.claims();
        userClaims.setSubject("cashier");
        userClaims.put("role", "CASHIER");

        @SuppressWarnings("unchecked")
        Jws<Claims> userJws = mock(Jws.class);
        when(userJws.getBody()).thenReturn(userClaims);
        when(jwtUtil.parseToken("user-token")).thenReturn(userJws);

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        "cashier", "pass",
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CASHIER"))
                );

        when(userDetailsService.loadUserByUsername("cashier")).thenReturn(userDetails);

        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }
}
