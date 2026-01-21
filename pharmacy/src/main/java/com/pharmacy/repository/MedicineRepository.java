package com.pharmacy.repository;

import com.pharmacy.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    @Query("SELECT m FROM Medicine m WHERE m.quantity < :threshold")
    List<Medicine> findLowStockMedicines(@Param("threshold") int threshold);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.quantity < :threshold")
    long countLowStockMedicines(@Param("threshold") int threshold);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.quantity = 0")
    long countOutOfStockMedicines();
}