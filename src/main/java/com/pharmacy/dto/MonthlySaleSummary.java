package com.pharmacy.dto;

import java.time.YearMonth;

public class MonthlySaleSummary {

    private YearMonth month;

    private long validSales;
    private long rejectedExpired;
    private long rejectedOutOfStock;
    private long totalSales;

    private double totalRevenue;

    // ✅ getters & setters

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    public long getValidSales() {
        return validSales;
    }

    public void setValidSales(long validSales) {
        this.validSales = validSales;
    }

    public long getRejectedExpired() {
        return rejectedExpired;
    }

    public void setRejectedExpired(long rejectedExpired) {
        this.rejectedExpired = rejectedExpired;
    }

    public long getRejectedOutOfStock() {
        return rejectedOutOfStock;
    }

    public void setRejectedOutOfStock(long rejectedOutOfStock) {
        this.rejectedOutOfStock = rejectedOutOfStock;
    }

    public long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(long totalSales) {
        this.totalSales = totalSales;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
