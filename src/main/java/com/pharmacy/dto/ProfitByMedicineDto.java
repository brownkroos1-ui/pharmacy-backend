package com.pharmacy.dto;

import java.math.BigDecimal;

public class ProfitByMedicineDto {
    private Long medicineId;
    private String medicineName;
    private Long quantitySold;
    private BigDecimal revenue;
    private BigDecimal cost;
    private BigDecimal profit;

    public ProfitByMedicineDto() {}

    public ProfitByMedicineDto(Long medicineId,
                               String medicineName,
                               Long quantitySold,
                               BigDecimal revenue,
                               BigDecimal cost) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
        this.cost = cost;
        this.profit = revenue.subtract(cost);
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public Long getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(Long quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}
