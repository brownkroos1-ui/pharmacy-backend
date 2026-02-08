package com.pharmacy.controller;

import com.pharmacy.dto.AdminDashboardDto;
import com.pharmacy.service.AdminDashboardService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService service;

    public AdminDashboardController(AdminDashboardService service) {
        this.service = service;
    }

    @GetMapping
    public AdminDashboardDto dashboard() {
        return service.getDashboardCounts();
    }
}
