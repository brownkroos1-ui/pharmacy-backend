package com.pharmacy.service;

import com.pharmacy.model.Supplier;
import com.pharmacy.repository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findByActiveTrue();
    }

    public Supplier getSupplier(Long id) {
        return supplierRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier with ID " + id + " not found"
                ));
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        if (supplier == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier data cannot be null");
        }
        validateNameUnique(supplier.getName(), null);
        supplier.setActive(true);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier details) {
        if (details == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier details cannot be null");
        }
        validateNameUnique(details.getName(), id);
        return supplierRepository.findByIdAndActiveTrue(id)
                .map(supplier -> {
                    supplier.setName(details.getName());
                    supplier.setPhone(details.getPhone());
                    supplier.setEmail(details.getEmail());
                    supplier.setAddress(details.getAddress());
                    return supplierRepository.save(supplier);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier with ID " + id + " not found"
                ));
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Supplier with ID " + id + " not found"
                ));
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    private void validateNameUnique(String name, Long currentId) {
        if (name == null || name.isBlank()) return;
        boolean exists = currentId == null
                ? supplierRepository.existsByNameIgnoreCase(name)
                : supplierRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Supplier name already exists");
        }
    }
}
