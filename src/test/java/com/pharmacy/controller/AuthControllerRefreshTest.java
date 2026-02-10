package com.pharmacy.controller;

import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import com.pharmacy.security.JwtUtil;
import com.pharmacy.security.SecurityConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerRefreshTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private com.pharmacy.security.CustomUserDetailsService userDetailsService;

    @Test
    void refreshReturnsNewTokensWhenValid() throws Exception {
        Claims claims = Jwts.claims().setSubject("refreshUser");
        claims.put("type", "refresh");
        @SuppressWarnings("unchecked")
        Jws<Claims> jws = Mockito.mock(Jws.class);
        when(jws.getBody()).thenReturn(claims);
        when(jwtUtil.parseToken("refresh-token")).thenReturn(jws);

        User user = new User();
        user.setUsername("refreshUser");
        user.setRole(Role.ADMIN);
        user.setActive(true);

        when(userRepository.findByUsername("refreshUser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("refreshUser", "ADMIN")).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken("refreshUser", "ADMIN")).thenReturn("new-refresh");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("new-access")))
                .andExpect(jsonPath("$.refreshToken", is("new-refresh")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    void refreshRejectsMissingToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshRejectsInvalidType() throws Exception {
        Claims claims = Jwts.claims().setSubject("refreshUser");
        claims.put("type", "access");
        @SuppressWarnings("unchecked")
        Jws<Claims> jws = Mockito.mock(Jws.class);
        when(jws.getBody()).thenReturn(claims);
        when(jwtUtil.parseToken("bad-token")).thenReturn(jws);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"bad-token\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRejectsDisabledUser() throws Exception {
        Claims claims = Jwts.claims().setSubject("disabledUser");
        claims.put("type", "refresh");
        @SuppressWarnings("unchecked")
        Jws<Claims> jws = Mockito.mock(Jws.class);
        when(jws.getBody()).thenReturn(claims);
        when(jwtUtil.parseToken("disabled-token")).thenReturn(jws);

        User user = new User();
        user.setUsername("disabledUser");
        user.setRole(Role.CASHIER);
        user.setActive(false);

        when(userRepository.findByUsername("disabledUser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"disabled-token\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshRejectsInvalidJwt() throws Exception {
        when(jwtUtil.parseToken("invalid-jwt"))
                .thenThrow(new JwtException("Invalid"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid-jwt\"}"))
                .andExpect(status().isUnauthorized());
    }
}
