package com.pharmacy.dto;

public enum ProfitPeriod {
    DAILY,
    WEEKLY,
    MONTHLY;

    public static ProfitPeriod from(String value) {
        if (value == null || value.isBlank()) {
            return DAILY;
        }
        return ProfitPeriod.valueOf(value.trim().toUpperCase());
    }
}
