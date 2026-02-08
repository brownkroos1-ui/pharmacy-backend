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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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

        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String name = request.getName() == null ? "" : request.getName().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();

        if (username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }

        log.info("Register attempt username={}, email={}", username, email == null ? "n/a" : email);

        userRepository.findByUsername(username)
                .ifPresent(u -> {
                    log.warn("Register failed: username already taken ({})", username);
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
                });

        if (email != null) {
            userRepository.findByEmail(email)
                    .ifPresent(u -> {
                        log.warn("Register failed: email already taken ({})", email);
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken");
                    });
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.CASHIER); // Default role for new users

        try {
            userRepository.save(newUser);
        } catch (Exception ex) {
            log.error("Register failed for username={}, email={}", username, email, ex);
            throw ex;
        }
        log.info("Register success username={}", username);

        String token = jwtUtil.generateToken(newUser.getUsername(), newUser.getRole().name());

        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(token, newUser.getRole().name()));
    }
}
