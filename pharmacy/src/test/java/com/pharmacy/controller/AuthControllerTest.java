package com.pharmacy.controller;

import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import com.pharmacy.security.JwtUtil;
import com.pharmacy.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

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
    void testRegisterEndpoint_CreatesUserAndReturnsToken() throws Exception {
        String username = "newuser";
        String password = "secret";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encoded-pass");
        when(jwtUtil.generateToken(eq(username), eq(Role.USER.name()))).thenReturn("token-123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", is("token-123")))
                .andExpect(jsonPath("$.role", is(Role.USER.name())));

        ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedCaptor.capture());

        User saved = savedCaptor.getValue();
        // Basic assertions about the saved user
        assert saved.getUsername().equals(username);
        assert saved.getPassword().equals("encoded-pass");
        assert saved.getRole() == Role.USER;
    }

    @Test
    void testLoginEndpoint_AllowsUnauthenticatedAccess() throws Exception {
        String username = "loginUser";
        String password = "pass";

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        username, password, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                );

        org.springframework.security.core.Authentication auth = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        org.mockito.Mockito.when(auth.getPrincipal()).thenReturn(userDetails);
        org.mockito.Mockito.when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any())).thenReturn(auth);

        org.mockito.Mockito.when(jwtUtil.generateToken(org.mockito.ArgumentMatchers.eq(username), org.mockito.ArgumentMatchers.eq(Role.USER.name())))
                .thenReturn("login-token-123");

        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("login-token-123")))
                .andExpect(jsonPath("$.role", is(Role.USER.name())));
    }

    @Test
    void testLoginEndpoint_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        org.mockito.Mockito.when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        String body = "{\"username\":\"bad\",\"password\":\"creds\"}";


        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Invalid username or password")));
    }
}
