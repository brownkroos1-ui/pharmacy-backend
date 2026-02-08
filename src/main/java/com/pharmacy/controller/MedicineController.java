package com.pharmacy.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.pharmacy.model.Medicine;
import com.pharmacy.service.MedicineService;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine createMedicine(@Valid @RequestBody Medicine medicine) {
        return medicineService.createMedicine(medicine);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public List<Medicine> getAllMedicines() {
        return medicineService.getAllMedicines();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public Medicine getMedicineById(@PathVariable Long id) {
        return medicineService.getMedicineById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine updateMedicine(@PathVariable Long id, @Valid @RequestBody Medicine medicine) {
        return medicineService.updateMedicine(id, medicine);
    }

    @PutMapping("/batch/{batchNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine updateMedicineByBatch(@PathVariable String batchNumber, @Valid @RequestBody Medicine medicine) {
        return medicineService.updateMedicineByBatch(batchNumber, medicine);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Medicine> getLowStockMedicines() {
        return medicineService.getLowStockMedicines();
    }

    @GetMapping("/threshold")
    public int getLowStockThreshold() {
        return medicineService.getLowStockThreshold();
    }
}
