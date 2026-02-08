package com.pharmacy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.pharmacy.dto.AdminUserDto;
import com.pharmacy.dto.CreateUserRequest;
import com.pharmacy.dto.ResetPasswordRequest;
import com.pharmacy.dto.UpdateUserRoleRequest;
import com.pharmacy.dto.UpdateUserStatusRequest;
import com.pharmacy.model.Role;
import com.pharmacy.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserDto> listUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active) {
        return adminUserService.listUsers(query, role, active);
    }

    @PostMapping
    public AdminUserDto createUser(@RequestBody CreateUserRequest request) {
        return adminUserService.createUser(request);
    }

    @PatchMapping("/{id}/role")
    public AdminUserDto updateRole(@PathVariable Long id,
                                   @RequestBody UpdateUserRoleRequest request,
                                   Authentication auth) {
        if (request == null || request.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        String actor = auth != null ? auth.getName() : null;
        return adminUserService.updateRole(id, request.getRole(), actor);
    }

    @PatchMapping("/{id}/status")
    public AdminUserDto updateStatus(@PathVariable Long id,
                                     @RequestBody UpdateUserStatusRequest request,
                                     Authentication auth) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }
        String actor = auth != null ? auth.getName() : null;
        return adminUserService.updateStatus(id, request.isActive(), actor);
    }

    @PatchMapping("/{id}/password")
    public AdminUserDto resetPassword(@PathVariable Long id,
                                      @RequestBody ResetPasswordRequest request,
                                      Authentication auth) {
        String actor = auth != null ? auth.getName() : null;
        return adminUserService.resetPassword(id, request, actor);
    }
}
