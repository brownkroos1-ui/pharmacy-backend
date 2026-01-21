package com.pharmacy.controller;

import com.pharmacy.dto.LoginRequest;
import com.pharmacy.dto.LoginResponse;
import com.pharmacy.dto.RegisterRequest;
import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import com.pharmacy.security.JwtUtil;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        String role = user.getAuthorities()
                          .iterator()
                          .next()
                          .getAuthority()
                          .replace("ROLE_", "");

        String token = jwtUtil.generateToken(user.getUsername(), role);

        return new LoginResponse(token, role);
    }

    @PostMapping("/register")
    public org.springframework.http.ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {

        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
                });

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.USER); // Default role for new users

        userRepository.save(newUser);

        String token = jwtUtil.generateToken(newUser.getUsername(), newUser.getRole().name());

        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(token, newUser.getRole().name()));
    }
}
