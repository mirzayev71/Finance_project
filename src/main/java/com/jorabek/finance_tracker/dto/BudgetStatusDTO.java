package com.jorabek.finance_tracker.dto;

public class BudgetStatusDTO {
    private Long id;
    private String category;
    private Double limitAmount;
    private Double spentAmount;
    private Double percentage;

    public BudgetStatusDTO(Long id, String category, Double limitAmount, Double spentAmount) {
        this.id = id;
        this.category = category;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount;

        if (limitAmount == null || limitAmount == 0) {
            this.percentage = (spentAmount > 0) ? 100.0 : 0.0;
        } else {
            this.percentage = (spentAmount / limitAmount) * 100;
        }
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public Double getLimitAmount() {
        return limitAmount;
    }

    public Double getSpentAmount() {
        return spentAmount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public double getSafePercentage() {
        if (Double.isNaN(percentage) || Double.isInfinite(percentage))
            return 0.0;
        return percentage;
    }

    // UI rangi uchun
    public String getColorClass() {
        double p = getSafePercentage();
        if (p >= 100)
            return "bg-danger";
        if (p >= 80)
            return "bg-warning";
        return "bg-success";
    }
}
