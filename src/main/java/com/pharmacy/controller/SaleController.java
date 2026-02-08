package com.pharmacy.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final long SUMMARY_CACHE_TTL_MS = 5 * 60 * 1000L;
    private final Map<String, CacheEntry> monthlySummaryCache = new ConcurrentHashMap<>();

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

        return buildMonthlySummary(YearMonth.of(year, month));
    }

    // ================= PROFIT =================

    @GetMapping("/profit/summary")
    public ProfitSummaryDto getProfitSummary(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        DateRange range = resolveRange(start, end);
        return saleService.getProfitSummary(range.start(), range.end());
    }

    @GetMapping("/profit/series")
    public List<ProfitPointDto> getProfitSeries(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "DAILY") String period) {
        DateRange range = resolveRange(start, end);
        ProfitPeriod profitPeriod = ProfitPeriod.from(period);
        return saleService.getProfitSeries(range.start(), range.end(), profitPeriod);
    }

    @GetMapping("/profit/top")
    public List<ProfitByMedicineDto> getTopProfitMedicines(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "5") int limit) {
        if (limit < 1 || limit > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "limit must be between 1 and 50"
            );
        }
        DateRange range = resolveRange(start, end);
        return saleService.getTopProfitMedicines(range.start(), range.end(), limit);
    }

    @GetMapping("/summary/monthly/range")
    public List<MonthlySaleSummary> getMonthlySummaryRange(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "6") int count) {

        if (count < 1 || count > 24) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "count must be between 1 and 24"
            );
        }

        String cacheKey = year + "-" + month + "-" + count;
        CacheEntry cached = monthlySummaryCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return new ArrayList<>(cached.data());
        }

        YearMonth end = YearMonth.of(year, month);
        List<MonthlySaleSummary> summaries = new ArrayList<>();
        for (int i = count - 1; i >= 0; i--) {
            summaries.add(buildMonthlySummary(end.minusMonths(i)));
        }
        monthlySummaryCache.put(cacheKey, new CacheEntry(summaries));
        return summaries;
    }

    private MonthlySaleSummary buildMonthlySummary(YearMonth ym) {
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

    private record CacheEntry(List<MonthlySaleSummary> data, long cachedAt) {
        CacheEntry(List<MonthlySaleSummary> data) {
            this(data, System.currentTimeMillis());
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > SUMMARY_CACHE_TTL_MS;
        }
    }

    private record DateRange(LocalDate start, LocalDate end) {}

    private DateRange resolveRange(String start, String end) {
        LocalDate endDate = end == null || end.isBlank()
                ? LocalDate.now()
                : LocalDate.parse(end);
        LocalDate startDate = start == null || start.isBlank()
                ? endDate.minusDays(29)
                : LocalDate.parse(start);
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "start must be on or before end"
            );
        }
        return new DateRange(startDate, endDate);
    }
}
