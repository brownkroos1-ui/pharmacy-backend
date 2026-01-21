package com.pharmacy.dto;

public record AdminDashboardDto(

    long totalUsers,
    long totalMedicines,
    long lowStockMedicines,
    long outOfStockMedicines,

    long totalSales,
    double todaySalesAmount,

    long completedSales,
    long cancelledSales
) {}
