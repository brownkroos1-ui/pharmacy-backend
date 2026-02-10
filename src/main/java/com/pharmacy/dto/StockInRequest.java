package com.pharmacy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockInRequest {
    private Long medicineId;
    private String batchNumber;
    private String name;
    private String category;
    private String manufacturer;
    private LocalDate expiryDate;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer reorderLevel;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String invoiceNumber;
    private String note;
}
