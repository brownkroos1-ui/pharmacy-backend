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
        return medicineRepository.findAll();
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine with ID " + id + " not found"));
    }

    @Transactional
    public Medicine createMedicine(Medicine medicine) {
        if (medicine == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medicine data cannot be null");
        }
        return medicineRepository.save(medicine);
    }

    @Transactional
    public Medicine updateMedicine(Long id, Medicine medicineDetails) {
        if (medicineDetails == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medicine details cannot be null");
        }
        return medicineRepository.findById(id)
                .map(medicine -> {
                    medicine.setName(medicineDetails.getName());
                    medicine.setCategory(medicineDetails.getCategory());
                    medicine.setPrice(medicineDetails.getPrice());
                    medicine.setQuantity(medicineDetails.getQuantity());
                    medicine.setExpiryDate(medicineDetails.getExpiryDate());
                    return medicineRepository.save(medicine);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine with ID " + id + " not found"));
    }

    @Transactional
    public void deleteMedicine(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine with ID " + id + " not found");
        }
        medicineRepository.deleteById(id);
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines(lowStockThreshold);
    }
}