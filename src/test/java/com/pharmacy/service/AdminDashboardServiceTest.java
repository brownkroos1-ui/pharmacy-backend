package com.pharmacy.service;

import com.pharmacy.dto.AdminDashboardDto;
import com.pharmacy.model.SaleStatus;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleRepository;
import com.pharmacy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private MedicineRepository medicineRepo;

    @Mock
    private SaleRepository saleRepo;

    @InjectMocks
    private AdminDashboardService service;

    @Test
    void getDashboardCounts_aggregatesExpectedValues() {
        when(userRepo.count()).thenReturn(42L);
        when(medicineRepo.countByActiveTrue()).thenReturn(100L);
        when(medicineRepo.countLowStockMedicines(10)).thenReturn(7L);
        when(medicineRepo.countOutOfStockMedicines()).thenReturn(3L);

        when(saleRepo.count()).thenReturn(200L);
        when(saleRepo.sumTotalPriceByStatusAndDateBetween(
                any(SaleStatus.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(BigDecimal.valueOf(1234.56));
        when(saleRepo.countByStatus(SaleStatus.VALID)).thenReturn(150L);
        when(saleRepo.countByStatus(SaleStatus.REJECTED_EXPIRED)).thenReturn(30L);
        when(saleRepo.countByStatus(SaleStatus.REJECTED_OUT_OF_STOCK)).thenReturn(20L);

        ReflectionTestUtils.setField(service, "lowStockThreshold", 10);

        AdminDashboardDto dto = service.getDashboardCounts();

        assertEquals(42L, dto.totalUsers());
        assertEquals(100L, dto.totalMedicines());
        assertEquals(7L, dto.lowStockMedicines());
        assertEquals(3L, dto.outOfStockMedicines());
        assertEquals(200L, dto.totalSales());
        assertEquals(1234.56, dto.todaySalesAmount(), 0.0001);
        assertEquals(150L, dto.completedSales());
        assertEquals(50L, dto.cancelledSales()); // 30 + 20
    }
}
