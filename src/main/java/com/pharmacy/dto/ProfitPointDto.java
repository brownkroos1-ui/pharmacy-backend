package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProfitPointDto {
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal revenue;
    private BigDecimal cost;
    private BigDecimal profit;
    private long saleCount;

    public ProfitPointDto() {}

    public ProfitPointDto(String label,
                          LocalDate startDate,
                          LocalDate endDate,
                          BigDecimal revenue,
                          BigDecimal cost,
                          BigDecimal profit,
                          long saleCount) {
        this.label = label;
        this.startDate = startDate;
        this.endDate = endDate;
        this.revenue = revenue;
        this.cost = cost;
        this.profit = profit;
        this.saleCount = saleCount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public long getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(long saleCount) {
        this.saleCount = saleCount;
    }
}
