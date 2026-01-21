package com.pharmacy.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.pharmacy.dto.*;
import com.pharmacy.model.*;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleRepository;
import com.pharmacy.service.SaleService;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleRepository saleRepository;
    private final SaleService saleService;

    public SaleController(SaleRepository saleRepository, SaleService saleService) {
        this.saleRepository = saleRepository;
        this.saleService = saleService;
    }

    // ================= SELL MEDICINE =================
    @PostMapping
    public Sale sellMedicine(@RequestBody SaleRequest request) {
        return saleService.createSale(request);
    }

    // ================= MANAGEMENT =================

    @GetMapping("/status/{status}")
    public List<Sale> getSalesByStatus(@PathVariable SaleStatus status) {
        return saleRepository.findByStatus(status);
    }

    @GetMapping("/summary")
    public SaleSummary getSalesSummary() {

        SaleSummary summary = new SaleSummary();

        long valid = saleRepository.countByStatus(SaleStatus.VALID);
        long expired = saleRepository.countByStatus(SaleStatus.REJECTED_EXPIRED);
        long out = saleRepository.countByStatus(SaleStatus.REJECTED_OUT_OF_STOCK);

        summary.setValidSales(valid);
        summary.setRejectedExpired(expired);
        summary.setRejectedOutOfStock(out);
        summary.setTotalSales(valid + expired + out);

        return summary;
    }

    @GetMapping("/summary/today")
    public DailySaleSummary getTodaySummary() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        DailySaleSummary summary = new DailySaleSummary();
        summary.setDate(today);

        summary.setValidSales(
                saleRepository.countByStatusAndSaleDateBetween(
                        SaleStatus.VALID, start, end));

        summary.setRejectedExpired(
                saleRepository.countByStatusAndSaleDateBetween(
                        SaleStatus.REJECTED_EXPIRED, start, end));

        summary.setRejectedOutOfStock(
                saleRepository.countByStatusAndSaleDateBetween(
                        SaleStatus.REJECTED_OUT_OF_STOCK, start, end));

        summary.setTotalSales(
                summary.getValidSales()
                        + summary.getRejectedExpired()
                        + summary.getRejectedOutOfStock());

        return summary;
    }

    @GetMapping("/summary/revenue/today")
    public DailyRevenueSummary getTodayRevenue() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<Sale> sales =
                saleRepository.findByStatusAndSaleDateBetween(
                        SaleStatus.VALID, start, end);

        double revenue = sales.stream()
                .map(Sale::getTotalPrice)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        DailyRevenueSummary summary = new DailyRevenueSummary();
        summary.setDate(today);
        summary.setTotalRevenue(revenue);
        summary.setValidSalesCount(sales.size());

        return summary;
    }

    @GetMapping("/summary/monthly")
    public MonthlySaleSummary getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);

        MonthlySaleSummary summary = new MonthlySaleSummary();
        summary.setMonth(ym);

        summary.setValidSales(
                saleRepository.countByStatusAndSaleDateBetween(
                        SaleStatus.VALID, start, end));

        summary.setRejectedExpired(
                saleRepository.countByStatusAndSaleDateBetween(
                        SaleStatus.REJECTED_EXPIRED, start, end));

        summary.setRejectedOutOfStock(
                saleRepository.countByStatusAndSaleDateBetween(
                        SaleStatus.REJECTED_OUT_OF_STOCK, start, end));

        summary.setTotalSales(
                saleRepository.countBySaleDateBetween(start, end));

        summary.setTotalRevenue(
                saleRepository.sumTotalPriceByStatusAndDateBetween(
                        SaleStatus.VALID, start, end).doubleValue());

        return summary;
    }
}
