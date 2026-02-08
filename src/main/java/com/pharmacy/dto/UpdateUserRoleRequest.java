package com.pharmacy.dto;

import com.pharmacy.model.Role;

public class UpdateUserRoleRequest {
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
