package com.pharmacy.dto;

public class SaleSummary {

    private long totalSales;
    private long validSales;
    private long rejectedExpired;
    private long rejectedOutOfStock;

    public long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(long totalSales) {
        this.totalSales = totalSales;
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
}
