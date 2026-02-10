package com.pharmacy.repository;

import com.pharmacy.model.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockInRepository extends JpaRepository<StockIn, Long> {
    List<StockIn> findAllByOrderByReceivedAtDesc();
}
