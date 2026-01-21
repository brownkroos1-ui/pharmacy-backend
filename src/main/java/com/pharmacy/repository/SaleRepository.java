package com.pharmacy.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleStatus;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    // ================== FIND ==================

    List<Sale> findByStatus(SaleStatus status);

    List<Sale> findByStatusAndSaleDateBetween(SaleStatus status, LocalDateTime start, LocalDateTime end);

    // ================== COUNT ==================

    long countByStatus(SaleStatus status);

    long countByStatusAndSaleDateBetween(
            SaleStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    long countBySaleDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // ================== SUM ==================

    @Query("""
        SELECT COALESCE(SUM(s.totalPrice), 0)
        FROM Sale s
        WHERE s.status = :status
        AND s.saleDate BETWEEN :start AND :end
    """)
    BigDecimal sumTotalPriceByStatusAndDateBetween(
            @Param("status") SaleStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
