package com.pharmacy.service;

import com.pharmacy.model.Medicine;
import com.pharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MedicineService {

    private final MedicineRepository medicineRepository;

    @Value("${pharmacy.low-stock-threshold:10}")
    private int lowStockThreshold;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
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
        return medicineRepository.save(medicine);
    }

    @Transactional
    public Medicine updateMedicine(Long id, Medicine medicineDetails) {
        if (medicineDetails == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medicine details cannot be null");
        }
        validatePricing(medicineDetails);
        validateBatchNumberUnique(medicineDetails.getBatchNumber(), id);
        return medicineRepository.findByIdAndActiveTrue(id)
                .map(medicine -> {
                    medicine.setName(medicineDetails.getName());
                    medicine.setCategory(medicineDetails.getCategory());
                    medicine.setManufacturer(medicineDetails.getManufacturer());
                    medicine.setBatchNumber(medicineDetails.getBatchNumber());
                    medicine.setPrice(medicineDetails.getPrice());
                    medicine.setCostPrice(medicineDetails.getCostPrice());
                    medicine.setQuantity(medicineDetails.getQuantity());
                    medicine.setReorderLevel(medicineDetails.getReorderLevel());
                    medicine.setExpiryDate(medicineDetails.getExpiryDate());
                    return medicineRepository.save(medicine);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine with ID " + id + " not found"));
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

        return medicineRepository.findByBatchNumber(batchNumber)
                .map(medicine -> {
                    Long id = medicine.getId();
                    String nextBatch = medicineDetails.getBatchNumber();
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
                    return medicineRepository.save(medicine);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine with batch " + batchNumber + " not found"));
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
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines(lowStockThreshold);
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }
}
