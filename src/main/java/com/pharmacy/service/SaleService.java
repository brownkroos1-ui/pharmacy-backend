package com.pharmacy.service;

import com.pharmacy.dto.SaleRequest;
import com.pharmacy.dto.ProfitByMedicineDto;
import com.pharmacy.dto.ProfitPeriod;
import com.pharmacy.dto.ProfitPointDto;
import com.pharmacy.dto.ProfitSummaryDto;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import com.pharmacy.model.SaleStatus;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaleService {

    private final MedicineRepository medicineRepository;
    private final SaleRepository saleRepository;
    private final AuditLogService auditLogService;

    public SaleService(MedicineRepository medicineRepository,
                       SaleRepository saleRepository,
                       AuditLogService auditLogService) {
        this.medicineRepository = medicineRepository;
        this.saleRepository = saleRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Sale createSale(SaleRequest request) {
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(request.getMedicineId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found"));

        Sale sale = new Sale();
        sale.setMedicine(medicine);
        sale.setQuantitySold(request.getQuantity());
        sale.setSaleDate(LocalDateTime.now());

        // Check Expiry
        if (medicine.getExpiryDate().isBefore(LocalDate.now())) {
            sale.setStatus(SaleStatus.REJECTED_EXPIRED);
            Sale saved = saleRepository.save(sale);
            auditLogService.log(
                    "SALE_REJECTED",
                    "SALE",
                    saved.getId(),
                    "Rejected expired sale for " + medicine.getName() + " (batch " + medicine.getBatchNumber() + ") qty " + request.getQuantity()
            );
            return saved;
        }

        // Check Stock
        if (medicine.getQuantity() < request.getQuantity()) {
            sale.setStatus(SaleStatus.REJECTED_OUT_OF_STOCK);
            Sale saved = saleRepository.save(sale);
            auditLogService.log(
                    "SALE_REJECTED",
                    "SALE",
                    saved.getId(),
                    "Rejected out-of-stock sale for " + medicine.getName() + " (batch " + medicine.getBatchNumber() + ") qty " + request.getQuantity()
            );
            return saved;
        }

        // Reduce Stock
        medicine.setQuantity(medicine.getQuantity() - request.getQuantity());
        medicineRepository.save(medicine);

        // Finalize Sale
        sale.setStatus(SaleStatus.VALID);
        Sale saved = saleRepository.save(sale);
        auditLogService.log(
                "SALE",
                "SALE",
                saved.getId(),
                "Sale for " + medicine.getName() + " (batch " + medicine.getBatchNumber() + ") qty "
                        + request.getQuantity() + " remaining " + medicine.getQuantity()
        );
        return saved;
    }

    public ProfitSummaryDto getProfitSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        var revenue = saleRepository.sumTotalPriceByStatusAndDateBetween(
                SaleStatus.VALID, start, end);
        var cost = saleRepository.sumTotalCostByStatusAndDateBetween(
                SaleStatus.VALID, start, end);
        var count = saleRepository.countByStatusAndSaleDateBetween(
                SaleStatus.VALID, start, end);

        var profit = revenue.subtract(cost);
        return new ProfitSummaryDto(startDate, endDate, revenue, cost, profit, count);
    }

    public List<ProfitPointDto> getProfitSeries(LocalDate startDate,
                                                LocalDate endDate,
                                                ProfitPeriod period) {
        List<ProfitPointDto> points = new ArrayList<>();
        LocalDate cursor = startDate;

        while (!cursor.isAfter(endDate)) {
            LocalDate segmentStart = cursor;
            LocalDate segmentEnd;
            String label;

            if (period == ProfitPeriod.WEEKLY) {
                segmentEnd = cursor.plusDays(6);
                label = segmentStart + " - " + segmentEnd;
                cursor = segmentEnd.plusDays(1);
            } else if (period == ProfitPeriod.MONTHLY) {
                YearMonth ym = YearMonth.from(cursor);
                segmentStart = ym.atDay(1);
                segmentEnd = ym.atEndOfMonth();
                label = ym.toString();
                cursor = segmentEnd.plusDays(1);
            } else {
                segmentEnd = cursor;
                label = segmentStart.toString();
                cursor = cursor.plusDays(1);
            }

            if (segmentEnd.isAfter(endDate)) {
                segmentEnd = endDate;
            }

            LocalDateTime start = segmentStart.atStartOfDay();
            LocalDateTime end = segmentEnd.atTime(23, 59, 59);

            var revenue = saleRepository.sumTotalPriceByStatusAndDateBetween(
                    SaleStatus.VALID, start, end);
            var cost = saleRepository.sumTotalCostByStatusAndDateBetween(
                    SaleStatus.VALID, start, end);
            var count = saleRepository.countByStatusAndSaleDateBetween(
                    SaleStatus.VALID, start, end);

            points.add(new ProfitPointDto(
                    label,
                    segmentStart,
                    segmentEnd,
                    revenue,
                    cost,
                    revenue.subtract(cost),
                    count
            ));
        }

        return points;
    }

    public List<ProfitByMedicineDto> getTopProfitMedicines(LocalDate startDate,
                                                           LocalDate endDate,
                                                           int limit) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        List<ProfitByMedicineDto> results = saleRepository.findProfitByMedicine(
                SaleStatus.VALID, start, end);
        if (results.size() <= limit) {
            return results;
        }
        return results.subList(0, limit);
    }
}
