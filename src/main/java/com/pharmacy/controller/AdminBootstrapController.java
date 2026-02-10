package com.pharmacy.controller;

import com.pharmacy.dto.AdminBootstrapResetRequest;
import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/bootstrap")
public class AdminBootstrapController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String secret;

    public AdminBootstrapController(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    @Value("${app.bootstrap.admin.enabled:true}") boolean enabled,
                                    @Value("${app.bootstrap.admin.secret:}") String secret) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.secret = secret;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        boolean hasAdmin = userRepository.existsByRole(Role.ADMIN);
        return Map.of(
                "enabled", enabled,
                "hasAdmin", hasAdmin
        );
    }

    @PostMapping("/reset")
    public Map<String, String> reset(@Valid @RequestBody AdminBootstrapResetRequest request) {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bootstrap is disabled");
        }
        if (secret == null || secret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bootstrap secret not configured");
        }
        if (!secret.equals(request.getSecret())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid bootstrap secret");
        }

        String username = request.getUsername().trim();
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User created = new User();
            created.setUsername(username);
            created.setRole(Role.ADMIN);
            created.setActive(true);
            return created;
        });

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        user.setActive(true);
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail().trim().toLowerCase());
        }
        userRepository.save(user);

        return Map.of("message", "Admin credentials reset");
    }
}
