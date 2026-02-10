package com.pharmacy.repository;

import com.pharmacy.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByActiveTrue();
    Optional<Supplier> findByIdAndActiveTrue(Long id);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
