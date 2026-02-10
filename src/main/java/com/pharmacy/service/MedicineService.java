package com.pharmacy.service;

import com.pharmacy.model.Medicine;
import com.pharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final AuditLogService auditLogService;

    @Value("${pharmacy.low-stock-threshold:10}")
    private int lowStockThreshold;

    public MedicineService(MedicineRepository medicineRepository,
                           AuditLogService auditLogService) {
        this.medicineRepository = medicineRepository;
        this.auditLogService = auditLogService;
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findByActiveTrue();
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine with ID " + id + " not found"));
    }

    @Transactional
    public Medicine createMedicine(Medicine medicine) {
        if (medicine == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medicine data cannot be null");
        }
        validatePricing(medicine);
        validateBatchNumberUnique(medicine.getBatchNumber(), null);
        Medicine saved = medicineRepository.save(medicine);
        auditLogService.log(
                "CREATE",
                "MEDICINE",
                saved.getId(),
                "Created medicine " + saved.getName() + " (batch " + saved.getBatchNumber() + ")"
        );
        return saved;
    }

    @Transactional
    public Medicine updateMedicine(Long id, Medicine medicineDetails) {
        if (medicineDetails == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medicine details cannot be null");
        }
        validatePricing(medicineDetails);
        validateBatchNumberUnique(medicineDetails.getBatchNumber(), id);
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine with ID " + id + " not found"));

        String changeSummary = describeMedicineChanges(medicine, medicineDetails);

        medicine.setName(medicineDetails.getName());
        medicine.setCategory(medicineDetails.getCategory());
        medicine.setManufacturer(medicineDetails.getManufacturer());
        medicine.setBatchNumber(medicineDetails.getBatchNumber());
        medicine.setPrice(medicineDetails.getPrice());
        medicine.setCostPrice(medicineDetails.getCostPrice());
        medicine.setQuantity(medicineDetails.getQuantity());
        medicine.setReorderLevel(medicineDetails.getReorderLevel());
        medicine.setExpiryDate(medicineDetails.getExpiryDate());

        Medicine saved = medicineRepository.save(medicine);
        auditLogService.log("UPDATE", "MEDICINE", saved.getId(), changeSummary);
        return saved;
    }

    @Transactional
    public Medicine updateMedicineByBatch(String batchNumber, Medicine medicineDetails) {
        if (batchNumber == null || batchNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Batch number is required");
        }
        if (medicineDetails == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medicine details cannot be null");
        }
        validatePricing(medicineDetails);

        Medicine medicine = medicineRepository.findByBatchNumber(batchNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine with batch " + batchNumber + " not found"));

        Long id = medicine.getId();
        String nextBatch = medicineDetails.getBatchNumber();
        String changeSummary = describeMedicineChanges(medicine, medicineDetails);

        if (nextBatch != null && !nextBatch.isBlank() && !nextBatch.equals(batchNumber)) {
            validateBatchNumberUnique(nextBatch, id);
            medicine.setBatchNumber(nextBatch);
        } else {
            medicine.setBatchNumber(batchNumber);
        }

        medicine.setName(medicineDetails.getName());
        medicine.setCategory(medicineDetails.getCategory());
        medicine.setManufacturer(medicineDetails.getManufacturer());
        medicine.setPrice(medicineDetails.getPrice());
        medicine.setCostPrice(medicineDetails.getCostPrice());
        medicine.setQuantity(medicineDetails.getQuantity());
        medicine.setReorderLevel(medicineDetails.getReorderLevel());
        medicine.setExpiryDate(medicineDetails.getExpiryDate());
        medicine.setActive(true);

        Medicine saved = medicineRepository.save(medicine);
        auditLogService.log("UPDATE", "MEDICINE", saved.getId(), changeSummary);
        return saved;
    }

    private void validatePricing(Medicine medicine) {
        if (medicine.getPrice() == null || medicine.getCostPrice() == null) {
            return;
        }
        if (medicine.getCostPrice().compareTo(medicine.getPrice()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cost price must be less than or equal to price"
            );
        }
    }

    private void validateBatchNumberUnique(String batchNumber, Long currentId) {
        if (batchNumber == null || batchNumber.isBlank()) {
            return;
        }
        boolean exists = currentId == null
                ? medicineRepository.existsByBatchNumber(batchNumber)
                : medicineRepository.existsByBatchNumberAndIdNot(batchNumber, currentId);
        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Batch number already exists"
            );
        }
    }

    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine with ID " + id + " not found"));
        medicine.setActive(false);
        medicineRepository.save(medicine);
        auditLogService.log(
                "DELETE",
                "MEDICINE",
                medicine.getId(),
                "Archived medicine " + medicine.getName() + " (batch " + medicine.getBatchNumber() + ")"
        );
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines(lowStockThreshold);
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    private String describeMedicineChanges(Medicine current, Medicine next) {
        List<String> changes = new ArrayList<>();
        if (!Objects.equals(current.getName(), next.getName())) {
            changes.add("name: " + current.getName() + " -> " + next.getName());
        }
        if (!Objects.equals(current.getCategory(), next.getCategory())) {
            changes.add("category: " + current.getCategory() + " -> " + next.getCategory());
        }
        if (!Objects.equals(current.getManufacturer(), next.getManufacturer())) {
            changes.add("manufacturer: " + current.getManufacturer() + " -> " + next.getManufacturer());
        }
        if (!Objects.equals(current.getBatchNumber(), next.getBatchNumber())) {
            changes.add("batch: " + current.getBatchNumber() + " -> " + next.getBatchNumber());
        }
        if (!Objects.equals(current.getPrice(), next.getPrice())) {
            changes.add("price: " + current.getPrice() + " -> " + next.getPrice());
        }
        if (!Objects.equals(current.getCostPrice(), next.getCostPrice())) {
            changes.add("cost: " + current.getCostPrice() + " -> " + next.getCostPrice());
        }
        if (!Objects.equals(current.getQuantity(), next.getQuantity())) {
            changes.add("qty: " + current.getQuantity() + " -> " + next.getQuantity());
        }
        if (!Objects.equals(current.getReorderLevel(), next.getReorderLevel())) {
            changes.add("reorder: " + current.getReorderLevel() + " -> " + next.getReorderLevel());
        }
        if (!Objects.equals(current.getExpiryDate(), next.getExpiryDate())) {
            changes.add("expiry: " + current.getExpiryDate() + " -> " + next.getExpiryDate());
        }
        if (changes.isEmpty()) {
            return "Updated medicine " + current.getId();
        }
        return "Updated medicine " + current.getId() + " (" + String.join(", ", changes) + ")";
    }
}
