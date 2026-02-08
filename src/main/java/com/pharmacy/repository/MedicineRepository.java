package com.pharmacy.repository;

import com.pharmacy.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    @Query("SELECT m FROM Medicine m WHERE m.active = true AND m.quantity > 0 AND m.quantity <= COALESCE(m.reorderLevel, :threshold)")
    List<Medicine> findLowStockMedicines(@Param("threshold") int threshold);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.active = true AND m.quantity > 0 AND m.quantity <= COALESCE(m.reorderLevel, :threshold)")
    long countLowStockMedicines(@Param("threshold") int threshold);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.active = true AND m.quantity = 0")
    long countOutOfStockMedicines();

    List<Medicine> findByActiveTrue();

    long countByActiveTrue();

    java.util.Optional<Medicine> findByIdAndActiveTrue(Long id);

    java.util.Optional<Medicine> findByBatchNumber(String batchNumber);

    boolean existsByBatchNumber(String batchNumber);

    boolean existsByBatchNumberAndIdNot(String batchNumber, Long id);
}
