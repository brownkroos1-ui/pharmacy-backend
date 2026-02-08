package com.pharmacy.service;

import com.pharmacy.dto.AdminDashboardDto;
import com.pharmacy.model.SaleStatus;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleRepository;
import com.pharmacy.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private final UserRepository userRepo;
    private final MedicineRepository medicineRepo;
    private final SaleRepository saleRepo;

    @Value("${pharmacy.low-stock-threshold:10}")
    private int lowStockThreshold;

    public AdminDashboardService(
            UserRepository userRepo,
            MedicineRepository medicineRepo,
            SaleRepository saleRepo) {

        this.userRepo = userRepo;
        this.medicineRepo = medicineRepo;
        this.saleRepo = saleRepo;
    }

    public AdminDashboardDto getDashboardCounts() {

        double todaySalesAmount = saleRepo.sumTotalPriceByStatusAndDateBetween(
                SaleStatus.VALID,
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(LocalTime.MAX)
        ).doubleValue();

        long completedSales = saleRepo.countByStatus(SaleStatus.VALID);
        long cancelledSales = saleRepo.countByStatus(SaleStatus.REJECTED_EXPIRED)
                + saleRepo.countByStatus(SaleStatus.REJECTED_OUT_OF_STOCK);

        return new AdminDashboardDto(
                userRepo.count(),
                medicineRepo.countByActiveTrue(),
                medicineRepo.countLowStockMedicines(lowStockThreshold),
                medicineRepo.countOutOfStockMedicines(),
                saleRepo.count(),
                todaySalesAmount,
                completedSales,
                cancelledSales
        );
    }
}
