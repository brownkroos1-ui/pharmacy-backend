package com.pharmacy.controller;

import com.pharmacy.dto.StockInRequest;
import com.pharmacy.model.StockIn;
import com.pharmacy.service.StockInService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-ins")
public class StockInController {

    private final StockInService stockInService;

    public StockInController(StockInService stockInService) {
        this.stockInService = stockInService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<StockIn> getStockIns() {
        return stockInService.getAllStockIns();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public StockIn createStockIn(@Valid @RequestBody StockInRequest request) {
        return stockInService.createStockIn(request);
    }
}
