package com.pharmacy.controller;

import com.pharmacy.model.Supplier;
import com.pharmacy.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Supplier> getSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Supplier getSupplier(@PathVariable Long id) {
        return supplierService.getSupplier(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Supplier createSupplier(@Valid @RequestBody Supplier supplier) {
        return supplierService.createSupplier(supplier);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Supplier updateSupplier(@PathVariable Long id, @Valid @RequestBody Supplier supplier) {
        return supplierService.updateSupplier(id, supplier);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
    }
}
