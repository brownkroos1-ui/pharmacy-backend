package com.pharmacy.dto;

import java.time.LocalDate;

public class DailyRevenueSummary {

    private LocalDate date;
    private double totalRevenue;
    private long validSalesCount;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getValidSalesCount() {
        return validSalesCount;
    }

    public void setValidSalesCount(long validSalesCount) {
        this.validSalesCount = validSalesCount;
    }
}
