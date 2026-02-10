package com.pharmacy.service;

import com.pharmacy.dto.StockInRequest;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.StockIn;
import com.pharmacy.model.Supplier;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.StockInRepository;
import com.pharmacy.repository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StockInService {

    private final StockInRepository stockInRepository;
    private final MedicineRepository medicineRepository;
    private final SupplierRepository supplierRepository;

    public StockInService(StockInRepository stockInRepository,
                          MedicineRepository medicineRepository,
                          SupplierRepository supplierRepository) {
        this.stockInRepository = stockInRepository;
        this.medicineRepository = medicineRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<StockIn> getAllStockIns() {
        return stockInRepository.findAllByOrderByReceivedAtDesc();
    }

    @Transactional
    public StockIn createStockIn(StockInRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock-in request cannot be null");
        }

        Supplier supplier = supplierRepository.findByIdAndActiveTrue(request.getSupplierId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier not found"
                ));

        Medicine medicine = resolveMedicine(request);

        validateCostVsPrice(request.getCostPrice(), request.getPrice(), medicine);

        int quantity = request.getQuantity();
        medicine.setQuantity((medicine.getQuantity() == null ? 0 : medicine.getQuantity()) + quantity);
        medicineRepository.save(medicine);

        StockIn stockIn = new StockIn();
        stockIn.setMedicine(medicine);
        stockIn.setSupplier(supplier);
        stockIn.setQuantity(quantity);
        stockIn.setUnitCost(request.getCostPrice() != null ? request.getCostPrice() : medicine.getCostPrice());
        stockIn.setInvoiceNumber(request.getInvoiceNumber());
        stockIn.setNote(request.getNote());
        stockIn.setReceivedAt(LocalDateTime.now());
        return stockInRepository.save(stockIn);
    }

    private Medicine resolveMedicine(StockInRequest request) {
        if (request.getMedicineId() != null) {
            return medicineRepository.findByIdAndActiveTrue(request.getMedicineId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found"));
        }

        String batchNumber = request.getBatchNumber();
        if (batchNumber != null && !batchNumber.isBlank()) {
            var existing = medicineRepository.findByBatchNumber(batchNumber);
            if (existing.isPresent()) {
                Medicine medicine = existing.get();
                applyUpdates(medicine, request);
                medicine.setActive(true);
                return medicineRepository.save(medicine);
            }
        }

        return createNewMedicine(request);
    }

    private Medicine createNewMedicine(StockInRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medicine name is required");
        }
        if (request.getBatchNumber() == null || request.getBatchNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Batch number is required");
        }
        if (request.getExpiryDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date is required");
        }
        if (request.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date cannot be in the past");
        }
        if (request.getPrice() == null || request.getCostPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price and cost price are required");
        }
        if (request.getCostPrice().compareTo(request.getPrice()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cost price must be <= price");
        }

        Medicine medicine = new Medicine();
        medicine.setName(request.getName());
        medicine.setCategory(request.getCategory());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setBatchNumber(request.getBatchNumber());
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setPrice(request.getPrice());
        medicine.setCostPrice(request.getCostPrice());
        medicine.setQuantity(0);
        medicine.setReorderLevel(request.getReorderLevel());
        medicine.setActive(true);
        return medicineRepository.save(medicine);
    }

    private void applyUpdates(Medicine medicine, StockInRequest request) {
        java.math.BigDecimal nextPrice = request.getPrice() != null ? request.getPrice() : medicine.getPrice();
        java.math.BigDecimal nextCost = request.getCostPrice() != null ? request.getCostPrice() : medicine.getCostPrice();
        if (nextCost != null && nextPrice != null && nextCost.compareTo(nextPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cost price must be <= price");
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            medicine.setName(request.getName());
        }
        if (request.getCategory() != null) {
            medicine.setCategory(request.getCategory());
        }
        if (request.getManufacturer() != null) {
            medicine.setManufacturer(request.getManufacturer());
        }
        if (request.getExpiryDate() != null) {
            medicine.setExpiryDate(request.getExpiryDate());
        }
        if (request.getPrice() != null) {
            medicine.setPrice(request.getPrice());
        }
        if (request.getCostPrice() != null) {
            medicine.setCostPrice(request.getCostPrice());
        }
        if (request.getReorderLevel() != null) {
            medicine.setReorderLevel(request.getReorderLevel());
        }
    }

    private void validateCostVsPrice(java.math.BigDecimal costPrice,
                                     java.math.BigDecimal requestPrice,
                                     Medicine medicine) {
        if (costPrice == null) return;
        java.math.BigDecimal priceToCompare =
                requestPrice != null ? requestPrice : medicine.getPrice();
        if (priceToCompare == null) return;
        if (costPrice.compareTo(priceToCompare) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cost price must be <= price");
        }
    }
}
