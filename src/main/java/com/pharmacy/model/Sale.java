package com.pharmacy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "quantity", nullable = false)
    private Integer quantitySold;
    private LocalDateTime saleDate;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private SaleStatus status;

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (status == SaleStatus.REJECTED_EXPIRED || status == SaleStatus.REJECTED_OUT_OF_STOCK) {
            this.totalPrice = BigDecimal.ZERO;
        } else if (medicine != null && medicine.getPrice() != null && quantitySold != null) {
            this.totalPrice = medicine.getPrice().multiply(BigDecimal.valueOf(quantitySold));
        }
    }
}
