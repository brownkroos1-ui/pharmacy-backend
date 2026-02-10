package com.pharmacy.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pharmacy.dto.AdminUserDto;
import com.pharmacy.dto.CreateUserRequest;
import com.pharmacy.dto.ResetPasswordRequest;
import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AdminUserService(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public List<AdminUserDto> listUsers(String query, Role role, Boolean active) {
        return userRepository.searchUsers(query, role, active).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AdminUserDto createUser(CreateUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User data is required");
        }
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String password = request.getPassword() != null ? request.getPassword().trim() : "";
        if (username.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        if (email != null && !email.isBlank() && userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(request.getName());
        user.setEmail(email == null || email.isBlank() ? null : email);
        user.setRole(request.getRole() == null ? Role.CASHIER : request.getRole());
        user.setActive(request.getActive() == null ? true : request.getActive());
        User saved = userRepository.save(user);
        auditLogService.log(
                "CREATE",
                "USER",
                saved.getId(),
                "Created user " + saved.getUsername() + " role " + saved.getRole()
        );
        return toDto(saved);
    }

    @Transactional
    public AdminUserDto updateRole(Long id, Role role, String actorUsername) {
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (actorUsername != null
                && user.getUsername() != null
                && user.getUsername().equalsIgnoreCase(actorUsername)
                && role != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot change your own role"
            );
        }
        Role previousRole = user.getRole();
        user.setRole(role);
        User saved = userRepository.save(user);
        auditLogService.log(
                "UPDATE_ROLE",
                "USER",
                saved.getId(),
                "Updated role for " + saved.getUsername() + " from " + previousRole + " to " + role,
                actorUsername
        );
        return toDto(saved);
    }

    @Transactional
    public AdminUserDto updateStatus(Long id, boolean active, String actorUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!active
                && actorUsername != null
                && user.getUsername() != null
                && user.getUsername().equalsIgnoreCase(actorUsername)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot disable your own account"
            );
        }
        boolean previous = user.isActive();
        user.setActive(active);
        User saved = userRepository.save(user);
        auditLogService.log(
                "UPDATE_STATUS",
                "USER",
                saved.getId(),
                "Updated status for " + saved.getUsername() + " from " + previous + " to " + active,
                actorUsername
        );
        return toDto(saved);
    }

    @Transactional
    public AdminUserDto resetPassword(Long id, ResetPasswordRequest request, String actorUsername) {
        if (request == null || request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        User saved = userRepository.save(user);
        auditLogService.log(
                "RESET_PASSWORD",
                "USER",
                saved.getId(),
                "Reset password for " + saved.getUsername(),
                actorUsername
        );
        return toDto(saved);
    }

    private AdminUserDto toDto(User user) {
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}
