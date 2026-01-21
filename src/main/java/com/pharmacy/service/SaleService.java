package com.pharmacy.service;

import com.pharmacy.dto.SaleRequest;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleStatus;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class SaleService {

    private final MedicineRepository medicineRepository;
    private final SaleRepository saleRepository;

    public SaleService(MedicineRepository medicineRepository, SaleRepository saleRepository) {
        this.medicineRepository = medicineRepository;
        this.saleRepository = saleRepository;
    }

    @Transactional
    public Sale createSale(SaleRequest request) {
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found"));

        Sale sale = new Sale();
        sale.setMedicine(medicine);
        sale.setQuantitySold(request.getQuantity());
        sale.setSaleDate(LocalDateTime.now());

        // Check Expiry
        if (medicine.getExpiryDate().isBefore(LocalDate.now())) {
            sale.setStatus(SaleStatus.REJECTED_EXPIRED);
            return saleRepository.save(sale);
        }

        // Check Stock
        if (medicine.getQuantity() < request.getQuantity()) {
            sale.setStatus(SaleStatus.REJECTED_OUT_OF_STOCK);
            return saleRepository.save(sale);
        }

        // Reduce Stock
        medicine.setQuantity(medicine.getQuantity() - request.getQuantity());
        medicineRepository.save(medicine);

        // Finalize Sale
        sale.setStatus(SaleStatus.VALID);
        return saleRepository.save(sale);
    }
}