package com.pharmacy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_ins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    private String note;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
}
